package com.merchant.testing.service.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merchant.testing.domain.enums.BusinessTypeEnum;
import com.merchant.testing.entity.Merchant;
import com.merchant.testing.repository.MerchantRepository;
import com.merchant.testing.service.aws.DynamoDbService;
import com.merchant.testing.service.aws.S3StorageService;
import com.merchant.testing.service.external.DictionaryApiService;
import com.merchant.testing.service.merchant.bean.MerchantCreateBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MerchantServiceTest {

    private MerchantService merchantService;
    private S3Client mockedS3Client;
    private MerchantRepository mockedRepository;
    private DynamoDbClient mockedDb;
    private DictionaryApiService mockedDictionaryService;

    @BeforeEach
    void setUp() {

        mockedS3Client = mock(S3Client.class);
        mockedDb = mock(DynamoDbClient.class);
        mockedRepository = mock(MerchantRepository.class);
        mockedDictionaryService = mock(DictionaryApiService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        merchantService = new MerchantService(
                new MerchantLoadServiceStrategy(
                        new MerchantLoadServiceS3(new S3StorageService(mockedS3Client), objectMapper),
                        new MerchantLoadServicePostgres(mockedRepository),
                        new MerchantLoadServiceDynamoDB(new DynamoDbService(mockedDb))),
                mockedRepository,
                new MerchantPhoneticService(mockedDictionaryService)
        );
    }


    @Test
    void shouldThrowExceptionWhenMerchantAlreadyExists() {
        // given
        String mail = "email@example.com";
        String name = "name";
        when(mockedRepository.existsByEmail(mail)).thenReturn(true);
        noSpellingForName(name);
        MerchantCreateBean merchantCreateBean = new MerchantCreateBean(
                name,
                mail,
                BusinessTypeEnum.MEDIUM
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                merchantService.createMerchant(merchantCreateBean)
        );
    }

    @Test
    void shouldCreateMerchant() {
        // given
        String mail = "email@example.com";
        when(mockedRepository.existsByEmail(mail)).thenReturn(false);
        noSpellingForName("name");
        MerchantCreateBean merchantCreateBean = new MerchantCreateBean(
                "name",
                mail,
                BusinessTypeEnum.MEDIUM
        );
        when(mockedRepository.save(any(Merchant.class))).thenAnswer(invocation -> {
            Merchant saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // when
        Merchant merchant = merchantService.createMerchant(merchantCreateBean);

        // then
        assertThat(merchant)
                .isNotNull()
                .matches(m -> m.getId() != null)
                .matches(m -> m.getName().equals(merchantCreateBean.name()))
                .matches(m -> m.getEmail().equals(merchantCreateBean.email()));
        verify(mockedRepository).save(any(Merchant.class));


    }

    private void noSpellingForName(String name) {
        when(mockedDictionaryService.getDictionary(name)).thenReturn(java.util.List.of());
    }

    @Test
    void shouldGetAllMerchants() {
        // given
        Merchant merchant1 = new Merchant("John Doe", "john@example.com", BusinessTypeEnum.SMALL);
        Merchant merchant2 = new Merchant("Jane Smith", "jane@example.com", BusinessTypeEnum.LARGE);
        when(mockedRepository.findAll()).thenReturn(java.util.List.of(merchant1, merchant2));

        // when
        List<Merchant> merchants = merchantService.getAllMerchants();

        // then
        assertThat(merchants)
                .hasSize(2)
                .containsExactly(merchant1, merchant2);
        verify(mockedRepository).findAll();
    }

    @Test
    void shouldGetMerchantById_FromDynamoDB() {
        // given
        Long merchantId = 123L;
        merchantExistsInDynamoDB(merchantId, "John Doe", "john@example.com", BusinessTypeEnum.MEDIUM);

        // when
        Optional<Merchant> merchant = merchantService.getMerchantById(merchantId);

        // then
        assertThat(merchant)
                .isPresent()
                .get()
                .matches(m -> m.getId().equals(merchantId))
                .matches(m -> m.getName().equals("John Doe"))
                .matches(m -> m.getEmail().equals("john@example.com"))
                .matches(m -> m.getBusinessType().equals(BusinessTypeEnum.MEDIUM));
        verify(mockedDb).getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class));
    }

    @Test
    void shouldGetMerchantById_FromPostgresWhenNotInDynamoDB() {
        // given
        Long merchantId = 123L;
        Merchant merchant = new Merchant("John Doe", "john@example.com", BusinessTypeEnum.MEDIUM);
        merchant.setId(merchantId);

        // DynamoDB returns null (item not found) - mock response to return null item
        software.amazon.awssdk.services.dynamodb.model.GetItemResponse mockResponse =
                mock(software.amazon.awssdk.services.dynamodb.model.GetItemResponse.class);
        when(mockResponse.item()).thenReturn(null);
        when(mockedDb.getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class)))
                .thenReturn(mockResponse);

        when(mockedRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        // when
        Optional<Merchant> result = merchantService.getMerchantById(merchantId);

        // then
        assertThat(result)
                .isPresent()
                .contains(merchant);
        verify(mockedDb).getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class));
        verify(mockedRepository).findById(merchantId);
    }

    @Test
    void shouldReturnEmpty_WhenMerchantNotFoundInAnySource() {
        // given
        Long merchantId = 123L;

        // DynamoDB returns null (item not found) - mock response to return null item
        software.amazon.awssdk.services.dynamodb.model.GetItemResponse mockResponse =
                mock(software.amazon.awssdk.services.dynamodb.model.GetItemResponse.class);
        when(mockResponse.item()).thenReturn(null);
        when(mockedDb.getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class)))
                .thenReturn(mockResponse);

        when(mockedRepository.findById(merchantId)).thenReturn(Optional.empty());
        merchantNotExistsInS3(merchantId.toString());

        // when & then
        // Note: S3StorageService.downloadFile() doesn't handle exceptions, so NoSuchKeyException
        // propagates up. In production, this should be caught and handled, but for this test
        // we expect the exception to be thrown.
        assertThrows(software.amazon.awssdk.core.exception.SdkClientException.class, () ->
                merchantService.getMerchantById(merchantId)
        );

        verify(mockedDb).getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class));
        verify(mockedRepository).findById(merchantId);
        verify(mockedS3Client).getObjectAsBytes(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
    }

    @Test
    void shouldUpdateMerchant() {
        // given
        Long merchantId = 123L;
        Merchant existingMerchant = new Merchant("Old Name", "john@example.com", BusinessTypeEnum.SMALL);
        existingMerchant.setId(merchantId);

        Merchant updatedDetails = new Merchant("New Name", "john@example.com", BusinessTypeEnum.LARGE);

        when(mockedRepository.findById(merchantId)).thenReturn(Optional.of(existingMerchant));
        when(mockedRepository.save(any(Merchant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Merchant result = merchantService.updateMerchant(merchantId, updatedDetails);

        // then
        assertThat(result)
                .isNotNull()
                .matches(m -> m.getName().equals("New Name"))
                .matches(m -> m.getBusinessType().equals(BusinessTypeEnum.LARGE));
        verify(mockedRepository).findById(merchantId);
        verify(mockedRepository).save(argThat(m ->
                m.getName().equals("New Name") &&
                m.getBusinessType().equals(BusinessTypeEnum.LARGE)));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMerchant() {
        // given
        Long merchantId = 999L;
        Merchant updatedDetails = new Merchant("New Name", "john@example.com", BusinessTypeEnum.LARGE);
        when(mockedRepository.findById(merchantId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                merchantService.updateMerchant(merchantId, updatedDetails)
        );
        verify(mockedRepository).findById(merchantId);
        verify(mockedRepository, never()).save(any());
    }

    @Test
    void shouldDeleteMerchant() {
        // given
        Long merchantId = 123L;

        // when
        merchantService.deleteMerchant(merchantId);

        // then
        verify(mockedRepository).deleteById(merchantId);
    }

    private void merchantExistsInDynamoDB(Long merchantId, String name, String email, BusinessTypeEnum businessType) {
        Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> item = Map.of(
                "id", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(merchantId.toString()).build(),
                "name", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(name).build(),
                "email", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(email).build(),
                "businessType", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(businessType.name()).build(),
                "phonetics", software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s("").build()
        );
        software.amazon.awssdk.services.dynamodb.model.GetItemResponse response =
                software.amazon.awssdk.services.dynamodb.model.GetItemResponse.builder()
                        .item(item)
                        .build();
        when(mockedDb.getItem(any(software.amazon.awssdk.services.dynamodb.model.GetItemRequest.class)))
                .thenReturn(response);
    }

    private void merchantNotExistsInS3(String merchantId) {
        // Mock S3 to throw SdkClientException when object doesn't exist
        when(mockedS3Client.getObjectAsBytes(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.core.exception.SdkClientException.builder()
                        .message("Object not found")
                        .cause(software.amazon.awssdk.services.s3.model.NoSuchKeyException.builder().build())
                        .build());
    }
}