package dev.mvc.openai;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

// Spring 기본 InputStreamResource는 getFilename()을 반환하지 않으므로 아래 클래스를 사용해야 합니다:
public class MultipartInputStreamFileResource extends InputStreamResource {
    private final String filename;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() {
        return -1; // 무시
    }
}


