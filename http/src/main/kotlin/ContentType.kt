sealed class ContentType(val type: String, open val subType: String) {

    val headerValue: String
        get() = "${type}/${subType}"

    companion object {
        const val APPLICATION = "application"
        const val AUDIO = "audio"
        const val IMAGE = "image"
        const val MULTIPART = "multipart"
        const val TEXT = "text"
        const val VIDEO = "video"
    }
}

data class ApplicationType(override val subType: String) : ContentType(APPLICATION, subType)

data class AudioType(override val subType: String) : ContentType(AUDIO, subType)

data class ImageType(override val subType: String) : ContentType(IMAGE, subType)

data class MultipartType(override val subType: String) : ContentType(MULTIPART, subType)

data class TextType(override val subType: String) : ContentType(TEXT, subType)

data class VideoType(override val subType: String) : ContentType(VIDEO, subType)

