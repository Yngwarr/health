function db_name {
    case "$1" in
        dev) export DB_NAME='health-dev' ;;
        test) export DB_NAME='health-test' ;;
    esac
}
