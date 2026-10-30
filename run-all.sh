#!/usr/bin/env bash
# run-all.sh - menjalankan seluruh service ABL di satu terminal (foreground).
#
# Pemakaian:
#   ./run-all.sh              jalankan semua service (Ctrl+C untuk berhenti)
#   ./run-all.sh status       cek status port tiap service
#   ./run-all.sh stop         matikan sisa proses di port service
#
# Catatan: RabbitMQ tidak dikelola script ini. Nyalakan manual bila perlu:
#   docker compose up -d

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# format: nama|folder|port|penanda_log
# port 0 berarti service tidak membuka port HTTP (hanya consumer AMQP),
# sehingga kesiapannya dideteksi dari baris log penanda.
SERVICES=(
  "eureka|ABL_Eureka|8761|"
  "auth|ABL_Auth|8084|"
  "product|ABL_Product|8080|"
  "pelanggan|ABL_Customer|8081|"
  "order|ABL_Order|8082|"
  "order-query|ABL_OrderQuery|8083|"
  "email|ABL_Email|0|Started EmailServiceApplication"
  "gateway|ABL_Gateway|9001|"
)

# Log & PID tiap service disimpan di sini agar bisa dicek/dimatikan tanpa
# mengganggu terminal.
LOG_DIR="$ROOT/.run-logs"
PID_DIR="$ROOT/.run-pids"

PIDS=()

# ---------------------------------------------------------------- util

port_open() {
  (exec 3<>"/dev/tcp/127.0.0.1/$1") >/dev/null 2>&1
}

port_pid() {
  netstat -ano 2>/dev/null | awk -v p=":$1 " '$0 ~ p && /LISTENING/ {print $NF; exit}'
}

# Membunuh proses Windows beserta seluruh anak-cucunya. Menerima Windows PID
# (yang dipakai netstat/tasklist), bukan PID MSYS.
kill_winpid() {
  local winpid=$1
  [ -z "$winpid" ] && return 0
  if command -v taskkill >/dev/null 2>&1; then
    taskkill //T //F //PID "$winpid" >/dev/null 2>&1 || true
  else
    kill -TERM "$winpid" 2>/dev/null || true
  fi
}

# Membunuh proses anak dari shell ini. Menerima PID MSYS lalu memetakannya
# ke Windows PID lewat /proc/<pid>/winpid.
kill_msys_pid() {
  local pid=$1 winpid
  [ -z "$pid" ] && return 0
  winpid=$(cat "/proc/$pid/winpid" 2>/dev/null || true)
  if [ -n "$winpid" ]; then
    kill_winpid "$winpid"
  else
    kill -TERM "$pid" 2>/dev/null || true
  fi
}

wait_for_port() {
  local name=$1 port=$2 limit=${3:-150} i=0
  while (( i < limit )); do
    if port_open "$port"; then
      printf '  [%-14s] UP    port %s\n' "$name" "$port"
      return 0
    fi
    sleep 1
    (( i++ ))
  done
  printf '  [%-14s] TIMEOUT setelah %ss (port %s)\n' "$name" "$limit" "$port" >&2
  return 1
}

# Untuk service tanpa port HTTP: tunggu baris penanda muncul di file log.
wait_for_log() {
  local name=$1 marker=$2 limit=${3:-150} i=0
  local file="$LOG_DIR/$name.log"
  while (( i < limit )); do
    if [ -f "$file" ] && grep -qF "$marker" "$file" 2>/dev/null; then
      printf '  [%-14s] UP    (log: %s)\n' "$name" "$marker"
      return 0
    fi
    sleep 1
    (( i++ ))
  done
  printf '  [%-14s] TIMEOUT setelah %ss (menunggu log: %s)\n' "$name" "$limit" "$marker" >&2
  return 1
}

start_service() {
  local name=$1 dir=$2
  mkdir -p "$LOG_DIR" "$PID_DIR"
  printf '  [%-14s] starting...\n' "$name"
  # exec supaya $! menunjuk proses mvnw, bukan subshell.
  # Tee: log utuh ke file (untuk readiness check) + prefix nama ke terminal.
  ( cd "$ROOT/$dir" && exec ./mvnw -DskipTests spring-boot:run ) \
    > >(tee "$LOG_DIR/$name.log" | sed -u "s/^/[$name] /") 2>&1 &
  PIDS+=("$!")
  # Simpan Windows PID pohon proses agar './run-all.sh stop' bisa mematikannya,
  # termasuk service tanpa port HTTP seperti email.
  cat "/proc/$!/winpid" > "$PID_DIR/$name.pid" 2>/dev/null || true
}

cleanup() {
  trap - INT TERM EXIT
  echo
  echo ">> Menghentikan semua service..."
  local pid name
  for pid in "${PIDS[@]}"; do
    kill_msys_pid "$pid"
  done
  # Sapu bersih: matikan sisa proses yang tercatat di PID file (mis. email
  # yang tidak punya port HTTP sehingga tidak terdeteksi lewat netstat).
  if [ -d "$PID_DIR" ]; then
    for f in "$PID_DIR"/*.pid; do
      [ -e "$f" ] || continue
      name=$(basename "$f" .pid)
      kill_winpid "$(cat "$f" 2>/dev/null)"
      printf '  [%-14s] dihentikan\n' "$name"
    done
  fi
  wait 2>/dev/null || true
  rm -rf "$PID_DIR"
  echo ">> Semua service berhenti."
}

# ---------------------------------------------------------------- subcommand

cmd_status() {
  local name dir port marker
  printf '%-16s %-6s %s\n' "SERVICE" "PORT" "STATUS"
  printf '%-16s %-6s %s\n' "----------------" "------" "--------"
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name dir port marker <<< "$entry"
    if [ "$port" = "0" ]; then
      printf '%-16s %-6s %s\n' "$name" "-" "(tanpa port HTTP)"
    elif port_open "$port"; then
      printf '%-16s %-6s %s\n' "$name" "$port" "UP"
    else
      printf '%-16s %-6s %s\n' "$name" "$port" "DOWN"
    fi
  done
}

cmd_stop() {
  local name dir port marker pid
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name dir port marker <<< "$entry"
    if [ "$port" = "0" ]; then
      # Service tanpa port HTTP: andalkan PID file yang ditulis saat start.
      if [ -f "$PID_DIR/$name.pid" ]; then
        kill_winpid "$(cat "$PID_DIR/$name.pid" 2>/dev/null)"
        rm -f "$PID_DIR/$name.pid"
        printf '%-16s dihentikan (pid file)\n' "$name"
      else
        printf '%-16s tidak ada pid file, dilewati\n' "$name"
      fi
      continue
    fi
    pid=$(port_pid "$port")
    if [ -z "$pid" ]; then
      printf '%-16s port %-5s sudah kosong\n' "$name" "$port"
      continue
    fi
    kill_winpid "$pid"
    printf '%-16s port %-5s dihentikan (pid %s)\n' "$name" "$port" "$pid"
  done
  rm -rf "$PID_DIR"
}

cmd_run() {
  local name dir port marker fail=0

  echo ">> Cek prasyarat..."
  if port_open 5672; then
    echo "   RabbitMQ OK (port 5672)"
  else
    echo "   ! RabbitMQ (port 5672) tidak terdeteksi."
    echo "     order-command, order-query, dan email butuh broker ini."
    echo "     Nyalakan manual: docker compose up -d"
  fi

  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name dir port marker <<< "$entry"
    [ "$port" = "0" ] && continue
    if port_open "$port"; then
      echo "   ! Port $port ($name) sudah dipakai. Jalankan './run-all.sh stop' dulu." >&2
      fail=1
    fi
  done
  if (( fail != 0 )); then
    exit 1
  fi
  echo "   Semua port bebas."
  echo

  rm -rf "$LOG_DIR" "$PID_DIR"
  mkdir -p "$LOG_DIR" "$PID_DIR"

  trap cleanup INT TERM EXIT

  echo ">> [1/3] Eureka"
  start_service eureka ABL_Eureka
  wait_for_port eureka 8761 120 || exit 1
  echo

  echo ">> [2/3] Service bisnis (paralel)"
  start_service auth          ABL_Auth
  start_service product       ABL_Product
  start_service pelanggan     ABL_Customer
  start_service order         ABL_Order
  start_service order-query   ABL_OrderQuery
  start_service email         ABL_Email
  wait_for_port auth          8084 180 || exit 1
  wait_for_port product       8080 180 || exit 1
  wait_for_port pelanggan     8081 180 || exit 1
  wait_for_port order         8082 180 || exit 1
  wait_for_port order-query   8083 180 || exit 1
  wait_for_log  email         "Started EmailServiceApplication" 180 || exit 1
  echo

  echo ">> [3/3] Gateway"
  start_service gateway ABL_Gateway
  wait_for_port gateway 9001 180 || exit 1
  echo

  cat <<EOF
============================================================
  Semua service berjalan. Tekan Ctrl+C untuk berhenti.
------------------------------------------------------------
  Gateway      : http://localhost:9001/api/...
  Eureka       : http://localhost:8761
  H2 Order     : http://localhost:8082/h2-console
  H2 Query     : http://localhost:8083/h2-console
  RabbitMQ     : http://localhost:15672  (admin/admin123)
------------------------------------------------------------
  Log lengkap  : $LOG_DIR/<nama-service>.log
  Cek status   : ./run-all.sh status   (dari terminal lain)
============================================================
EOF

  wait
}

case "${1:-run}" in
  run|"") cmd_run ;;
  status) cmd_status ;;
  stop)   cmd_stop ;;
  *)
    echo "Pemakaian: $0 [run|status|stop]" >&2
    exit 1
    ;;
esac
