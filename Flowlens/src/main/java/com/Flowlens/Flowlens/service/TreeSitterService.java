package com.Flowlens.Flowlens.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.treesitter.TreeSitterJava;

import com.Flowlens.Flowlens.model.CodeChunk;

import jakarta.annotation.PostConstruct;

import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TSNode;

@Service
public class TreeSitterService {
    RepositoryManager repositoryManager;
    TreeSitterService(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    // @PostConstruct
    // public void onStartup() {
    //     try {
    //         parseFile("rootlytic_dashboard_API", "rootlytic\\src\\main\\java\\com\\project\\rootlytic\\configuration\\SecurityConfig.java");
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    private Set<String> extractInvocations(TSNode node, String source) {
        Set<String> invocations = new HashSet<>();

        if (node.getType().equals("method_invocation")) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode != null) {
                String methodName = source.substring(nameNode.getStartByte(), nameNode.getEndByte());
                invocations.add(methodName);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            invocations.addAll(extractInvocations(node.getChild(i), source));
        }

        return invocations;
    }

    public List<CodeChunk> getCodeChunks(TSNode node, String source) {
        List<CodeChunk> chunks = new ArrayList<>();

        if (node.getType().equals("method_declaration")) {
            TSNode methodNameNode = node.getChildByFieldName("name");
            if (methodNameNode != null) {
                String methodName = source.substring(methodNameNode.getStartByte(), methodNameNode.getEndByte());
                Set<String> calledMethods = extractInvocations(node, source);
                String content = source.substring(node.getStartByte(), node.getEndByte());

                CodeChunk chunk = CodeChunk.builder()
                        .methodName(methodName)
                        .content(content)
                        .calledMethods(calledMethods)
                        .build();

                chunks.add(chunk);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            chunks.addAll(getCodeChunks(node.getChild(i), source));
        }

        return chunks;
    }

    public List<CodeChunk> parseFile(String repoName, String fileName) {
        TSParser parser = null;
        try {
            String fileContent = repositoryManager.readFileContent(repoName, fileName);
            parser = new TSParser();
            parser.setLanguage(new TreeSitterJava());
            TSTree tree = parser.parseString(null, fileContent);
            TSNode rootNode = tree.getRootNode();
            return getCodeChunks(rootNode, fileContent);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }
}
