package com.xinra.reviewcommunity.service;

import com.xinra.reviewcommunity.entity.Brand;
import com.xinra.reviewcommunity.entity.Category;
import com.xinra.reviewcommunity.entity.Product;
import com.xinra.reviewcommunity.repo.BrandRepository;
import com.xinra.reviewcommunity.repo.CategoryRepository;
import com.xinra.reviewcommunity.repo.ProductRepository;
import com.xinra.reviewcommunity.shared.dto.BrandDto;
import com.xinra.reviewcommunity.shared.dto.CreateProductDto;
import com.xinra.reviewcommunity.shared.dto.ProductDto;
import com.xinra.reviewcommunity.shared.dto.SerialDto;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ProductService extends AbstractService {

  private @Autowired ProductRepository<Product> productRepo;
  private @Autowired CategoryRepository<Category> categoryRepo;
  private @Autowired BrandRepository<Brand> brandRepo;

  /**
   * Creates a new product.
   */
  public SerialDto createProduct(@NonNull CreateProductDto createProductDto) {

    Category category = categoryRepo.findBySerial(createProductDto.getCategorySerial());

    if (category == null) {
      throw new SerialNotFoundException(Category.class, createProductDto.getCategorySerial());
    }

    Product product = entityFactory.createEntity(Product.class);
    product.setName(createProductDto.getName());
    product.setDescription(createProductDto.getDescription());
    product.setCategory(category);
    
    if (createProductDto.getBrandSerial() != 0) {
      Brand brand = brandRepo.findBySerial(createProductDto.getBrandSerial());
      if (brand == null) {
        throw new SerialNotFoundException(Brand.class, createProductDto.getBrandSerial());
      }
      product.setBrand(brand);
    }
    
    BarcodeService barcodeService = null;
    if (createProductDto.getBarcode() != null) {
      barcodeService = serviceProvider.getService(BarcodeService.class);
      // Make sure that creating the barcode will not fail before acquiring a serial as actually
      // creating it must be done afterwards
      barcodeService.checkBarcodeDoesNotExist(createProductDto.getBarcode());
    }

    int serial = serviceProvider.getService(SerialService.class).getNextSerial(Product.class);
    product.setSerial(serial);
    product = productRepo.save(product);
    
    if (createProductDto.getBarcode() != null) {
      // this must be called after saving the product
      barcodeService.setBarcodeOfProduct(product, createProductDto.getBarcode());
    }

    log.info("Created Product with name '{}'", createProductDto.getName());

    SerialDto serialDto = dtoFactory.createDto(SerialDto.class);
    serialDto.setSerial(serial);

    return serialDto;
  }

  /**
   * Returns the product with the given Serial.
   */
  public ProductDto getProductBySerial(int serial) {
    Product product = productRepo.findBySerial(serial);

    if (product == null) {
      throw new SerialNotFoundException(Product.class, serial);
    }
    return toDto(product);
  }

  /**
   * Returns a list of all products of a brand.
   */
  public List<ProductDto> getProductsByBrand(int serial) {
    List<ProductDto> list = productRepo.findByBrandSerial(serial).stream()
        .map(this::toDto)
        .collect(Collectors.toList());

    if (list.isEmpty()) {
      throw new SerialNotFoundException(Brand.class, serial);
    }
    return list;
  }

  /**
   * Returns a list of all products of a category.
   */
  public List<ProductDto> getProductsByCategory(int categorySerial) {
    Category category = categoryRepo.findBySerial(categorySerial);
    
    if (category == null) {
      throw new SerialNotFoundException(Category.class, categorySerial);
    }
    
    Set<String> categoryIds = getTransitiveCategoryIds(category.getPk().getId());
    
    return productRepo.findByCategoryIds(categoryIds).stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }
  
  /**
   * Returns the IDs of all categories that are included by the given one (itself and all transitive
   * children).
   */
  private Set<String> getTransitiveCategoryIds(String categoryId) {
    Set<String> transitiveCategoryIds = new HashSet<>();
    transitiveCategoryIds.add(categoryId);
    categoryRepo.getIdsByParentId(categoryId).forEach(childId ->
        transitiveCategoryIds.addAll(getTransitiveCategoryIds(childId)));
    return transitiveCategoryIds;
  }

  /**
   * Converts a product entity to a DTO.
   */
  public ProductDto toDto(Product product) {

    ProductDto productDto = dtoFactory.createDto(ProductDto.class);

    productDto.setSerial(product.getSerial());
    productDto.setName(product.getName());
    productDto.setDescription(product.getDescription());
    productDto.setNumRatings(product.getNumRatings());
    productDto.setAvgRating(product.getAvgRating());

    if (product.getBrand() != null) {
      BrandDto brandDto = dtoFactory.createDto(BrandDto.class);
      brandDto.setName(product.getBrand().getName());
      brandDto.setSerial(product.getBrand().getSerial());
      productDto.setBrand(brandDto);
    }

    productDto.setCategorySerial(product.getCategory().getSerial());

    return productDto;
  }
}

