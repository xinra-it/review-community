package com.xinra.reviewcommunity.rest;

import com.xinra.reviewcommunity.dto.CreateProductDto;
import com.xinra.reviewcommunity.dto.ProductDto;
import com.xinra.reviewcommunity.service.ProductService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController extends AbstractController {


  /**
   * Create a new product.
   */
  @RequestMapping(path = "", method = RequestMethod.POST)
  public void create(CreateProductDto createProductDto) {
    serviceProvider.getService(ProductService.class).createProduct(createProductDto);
  }

  /**
   * GET a product by Serial.
   */
  @RequestMapping(path = "/{serial}", method = RequestMethod.GET)
  public ProductDto get(@PathVariable int serial) {
    return serviceProvider.getService(ProductService.class).getProductBySerial(serial);
  }
}
