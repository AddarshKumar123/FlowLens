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

    public String findClass(TSNode node, String source) {
        TSNode current = node;
        while (current != null && !current.isNull()) {
            if (current.getType().equals("class_declaration")) {
                TSNode className = current.getChildByFieldName("name");
                if (className != null) {
                    return source.substring(className.getStartByte(), className.getEndByte());
                }
            }
            current = current.getParent();
        }
        return "";
    }

    public String findPackage(TSNode node, String source) {
        TSNode current = node;
        while (current != null && !current.isNull()) {
            if (current.getType().equals("package_declaration") || current.getType().equals("package")) {
                TSNode packageName = current.getChildByFieldName("name");
                if (packageName != null) {
                    return source.substring(packageName.getStartByte(), packageName.getEndByte());
                }
            }
            current = current.getParent();
        }
        return "";
    }

    private String findPackageFromRoot(TSNode node, String source) {
        if (!node.isNull() && (node.getType().equals("package_declaration"))) {
            System.out.println("Package declaration found with " + node.getChildCount() + " children");
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                System.out.println("Child " + i + " type: " + child.getType());
                if (!child.isNull() && !child.getType().equals("package") && !child.getType().equals(";")) {
                    return source.substring(child.getStartByte(), child.getEndByte());
                }
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TSNode child = node.getChild(i);
            if (!child.isNull()) {
                String pkg = findPackageFromRoot(child, source);
                if (!pkg.isEmpty()) {
                    return pkg;
                }
            }
        }
        return "";
    }

    public List<CodeChunk> getCodeChunks(TSNode node, String source) {
        return getCodeChunks(node, source, findPackageFromRoot(node, source));
    }

    private List<CodeChunk> getCodeChunks(TSNode node, String source, String packageName) {
        List<CodeChunk> chunks = new ArrayList<>();
        if (node.getType().equals("method_declaration")) {
            TSNode methodNameNode = node.getChildByFieldName("name");
            if (methodNameNode != null) {
                String methodName = source.substring(methodNameNode.getStartByte(), methodNameNode.getEndByte());
                Set<String> calledMethods = extractInvocations(node, source);
                String content = source.substring(node.getStartByte(), node.getEndByte());
                String className=findClass(methodNameNode,source);

                CodeChunk chunk = CodeChunk.builder()
                        .className(className)
                        .methodName(methodName)
                        .content(content)
                        .calledMethods(calledMethods)
                        .packageName(packageName)
                        .build();

                chunks.add(chunk);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            chunks.addAll(getCodeChunks(node.getChild(i), source, packageName));
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
