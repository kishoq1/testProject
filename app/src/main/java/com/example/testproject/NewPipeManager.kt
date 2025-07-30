package com.example.testproject



import android.content.Context
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException

// Lớp Downloader tùy chỉnh để hoạt động với OkHttp hoặc thư viện mạng khác
class CustomDownloader private constructor() : Downloader() {
    // Triển khai các phương thức của Downloader ở đây nếu cần.
    // Với mục đích cơ bản, bản triển khai mặc định có thể là đủ.
    // Ví dụ cơ bản nhất:
    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        // Bạn có thể tích hợp thư viện mạng như OkHttp ở đây
        // Để đơn giản, chúng ta sẽ dựa vào triển khai mặc định nếu có thể
        // hoặc để trống nếu bạn không cần tùy chỉnh sâu.
        // Đây là phần nâng cao, với mục đích chính là get link thì chưa cần phức tạp
        throw UnsupportedOperationException("Custom downloader not fully implemented")
    }

    companion object {
        val instance by lazy { CustomDownloader() }
    }
}


// Singleton để khởi tạo NewPipe
object NewPipeManager {
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        NewPipe.init(CustomDownloader.instance) // Sử dụng Downloader mặc định hoặc tùy chỉnh
        isInitialized = true
    }
}