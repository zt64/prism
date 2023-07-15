enum class Atom {
    IPC_CLOSE_CLIENT,
    IPC_ICONIFY_CLIENT,
    IPC_MAXIMIZE_CLIENT,
    IPC_TOGGLE_CLIENT_FULLSCREEN,
    IPC_EXIT,
    IPC_LIST_CLIENTS_REQUEST,
    IPC_LIST_CLIENTS_RESPONSE;

    companion object {
        fun from(value: Int) = entries[value]
    }
}