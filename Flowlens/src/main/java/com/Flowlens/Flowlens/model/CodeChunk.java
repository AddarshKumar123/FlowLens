package com.Flowlens.Flowlens.model;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CodeChunk {
    private String id;

    private String filePath;

    private String className;

    private String methodName;

    private String content;

    private Set<String> calledMethods;

    private int startLine;

    private int endLine;
}
