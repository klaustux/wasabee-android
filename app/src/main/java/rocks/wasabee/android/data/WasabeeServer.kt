package rocks.wasabee.android.data

enum class WasabeeServer(val label: String, val host: String) {
    US("America", "am"),
    EU("Europe", "eu"),
    APAC("Asia/Pacific", "ap");

    val baseUrl: String get() = "https://$host.wasabee.rocks"
}
