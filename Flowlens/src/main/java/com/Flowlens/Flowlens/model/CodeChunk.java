package com.Flowlens.Flowlens.model;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CodeChunk {

    private String id;

    private String repoName;

    private String filePath;

    private String packageName;

    private String className;

    private String methodName;

    private String signature;

    private String content;

    private Set<String> calledMethods;

    private List<String> imports;

    private int startLine;

    private int endLine;
}
