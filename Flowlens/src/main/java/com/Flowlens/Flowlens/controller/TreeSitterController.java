package com.Flowlens.Flowlens.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Flowlens.Flowlens.model.CodeChunk;
import com.Flowlens.Flowlens.service.TreeSitterService;

@RestController
public class TreeSitterController {
    @Autowired
    TreeSitterService treeSitterService;
    
    @GetMapping("/tree-sitter")
    public List<CodeChunk> getTreeSitter() {
        return treeSitterService.parseFile("rootlytic_dashboard_API", "rootlytic\\src\\main\\java\\com\\project\\rootlytic\\configuration\\SecurityConfig.java");
    }
}
