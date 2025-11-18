package com.merchant.testing.controller;

import com.merchant.testing.controller.bean.MerchantResponseBean;
import com.merchant.testing.entity.Merchant;
import com.merchant.testing.service.merchant.MerchantService;
import com.merchant.testing.service.merchant.bean.MerchantCreateBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<MerchantResponseBean> createMerchant(@RequestBody MerchantCreateBean merchant) {
        try {
            Merchant createdMerchant = merchantService.createMerchant(merchant);
            return ResponseEntity.status(HttpStatus.CREATED).body(MerchantResponseBean.from(createdMerchant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MerchantResponseBean>> getAllMerchants() {
        List<MerchantResponseBean> merchants = merchantService.getAllMerchants()
                .stream()
                .map(MerchantResponseBean::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponseBean> getMerchantById(@PathVariable Long id) {
        return merchantService.getMerchantById(id)
                .map(MerchantResponseBean::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<MerchantResponseBean> getMerchantByEmail(@PathVariable String email) {
        return merchantService.getMerchantByEmail(email)
                .map(MerchantResponseBean::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MerchantResponseBean> updateMerchant(
            @PathVariable Long id,
            @RequestBody Merchant merchantDetails) {
        try {
            Merchant updatedMerchant = merchantService.updateMerchant(id, merchantDetails);
            return ResponseEntity.ok(MerchantResponseBean.from(updatedMerchant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.noContent().build();
    }
}
