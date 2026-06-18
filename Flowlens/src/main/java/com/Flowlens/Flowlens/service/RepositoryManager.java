package com.Flowlens.Flowlens.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RepositoryManager {
    private String storagePath="C:\\Users\\Addarsh Kumar\\storage\\repos";

    @PostConstruct
    public void onStartup() {
        try {
            List<Path> files = getAllFiles(Path.of(storagePath, "rootlytic_dashboard_API"));
            System.out.println(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Git cloneRepository(String url,String repoName) throws Exception {
        File repoDir = Path.of(storagePath, repoName).toFile();
        if(repoDir.exists()) {
            return openRepository(repoName);
        }
        return Git.cloneRepository().setURI(url).setDirectory(repoDir).call();
    }

    public Git openRepository(String repoName) throws Exception {
        File repoDir = Path.of(storagePath, repoName).toFile();
        return Git.open(repoDir);
    }

    public List<Path> getAllFiles(Path repoRoot) throws Exception {
        try(Stream<Path>stream = Files.walk(repoRoot)) {
            return stream.filter(Files::isRegularFile)
            .filter(path -> !path.toString().contains(".git"))
            .filter(path -> path.toString().endsWith(".java"))
            .toList();
        }
    }

    public String readFileContent(String repoName, String fileName) throws Exception {
        Path filePath = Path.of(storagePath, repoName, fileName);
        return Files.readString(filePath);
    }
}
