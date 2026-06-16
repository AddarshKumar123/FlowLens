package com.Flowlens.Flowlens.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class codeChuck {
    private String id;

    private String filePath;

    private String className;

    private String methodName;

    private String content;

    private int startLine;

    private int endLine;
}
