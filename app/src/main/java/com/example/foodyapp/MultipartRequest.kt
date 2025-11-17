package com.example.foodyapp

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.util.UUID

class MultipartRequest(
    private val url: String,
    private val params: Map<String, String>,
    private val fileKey: String,
    private val fileData: ByteArray,
    private val fileName: String,
    private val listener: Response.Listener<String>,
    errorListener: Response.ErrorListener
) : Request<String>(Method.POST, url, errorListener) {

    private val boundary = "----" + UUID.randomUUID().toString()

    override fun getBodyContentType(): String =
        "multipart/form-data; boundary=$boundary"

    override fun getParams(): MutableMap<String, String> {
        return params.toMutableMap()
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()

        // 🔥 Parámetros de texto
        for ((key, value) in params) {
            bos.write("--$boundary\r\n".toByteArray())
            bos.write("Content-Disposition: form-data; name=\"$key\"\r\n\r\n".toByteArray())
            bos.write("$value\r\n".toByteArray())
        }

        // 🔥 Archivo
        bos.write("--$boundary\r\n".toByteArray())
        bos.write(
            "Content-Disposition: form-data; name=\"$fileKey\"; filename=\"$fileName\"\r\n"
                .toByteArray()
        )
        bos.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
        bos.write(fileData)
        bos.write("\r\n".toByteArray())

        bos.write("--$boundary--\r\n".toByteArray())

        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        val parsed = String(response.data)
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: String) {
        listener.onResponse(response)
    }
}
