package com.twog.shopping.domain.support.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.twog.shopping.domain.support.dto.CsTicketRequest;
import com.twog.shopping.domain.support.dto.CsTicketResponse;
import com.twog.shopping.domain.support.service.CsTicketService;


@RestController
@RequestMapping("/api/v1/cs-tickets")
public class CsTicketController {

    private final CsTicketService csTicketService;

    public CsTicketController(CsTicketService csTicketService) {
        this.csTicketService = csTicketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CsTicketResponse createTicket(@RequestBody CsTicketRequest req) {
        return csTicketService.createTicket(req);
    }

    @GetMapping
    public Page<CsTicketResponse> getMyTickets(
            @RequestParam Long memberId,
            @PageableDefault(size = 10, sort = "csTicketCreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return csTicketService.getMyTickets(memberId, pageable);
    }

    @GetMapping("/{id}")
    public CsTicketResponse getTicket(@PathVariable Long id) {
        return csTicketService.getTicket(id);
    }
}
