package com.example.campaignserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("campaign")
public class CampaignController {

    @GetMapping("/testPrint")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello  and Test312");
    }
}
