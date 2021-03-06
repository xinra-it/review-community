package com.xinra.reviewcommunity;

import com.xinra.nucleus.common.ContextHolder;
import com.xinra.nucleus.service.ServiceProvider;
import com.xinra.reviewcommunity.dto.AuthenticatedUserDto;
import com.xinra.reviewcommunity.entity.User;
import com.xinra.reviewcommunity.repo.UserRepository;
import com.xinra.reviewcommunity.service.AuthenticationProviderImpl;
import com.xinra.reviewcommunity.service.BrandService;
import com.xinra.reviewcommunity.service.CategoryService;
import com.xinra.reviewcommunity.service.MarketService;
import com.xinra.reviewcommunity.service.ProductService;
import com.xinra.reviewcommunity.service.ReviewService;
import com.xinra.reviewcommunity.service.UserService;
import com.xinra.reviewcommunity.shared.Role;
import com.xinra.reviewcommunity.shared.dto.CreateBrandDto;
import com.xinra.reviewcommunity.shared.dto.CreateCategoryDto;
import com.xinra.reviewcommunity.shared.dto.CreateProductDto;
import com.xinra.reviewcommunity.shared.dto.CreateReviewCommentDto;
import com.xinra.reviewcommunity.shared.dto.CreateReviewDto;
import com.xinra.reviewcommunity.shared.dto.DtoFactory;
import com.xinra.reviewcommunity.shared.dto.MarketDto;
import com.xinra.reviewcommunity.shared.dto.ReviewVoteDto;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"test", "dev"})
public class SampleContentGenerator implements ApplicationListener<ContextRefreshedEvent>, Ordered {
  
  private static final String USERNAME_ADMIN = "justus";
  private static final String USERNAME_MODERATOR = "peter";
  private static final String USERNAME_USER = "bob";
  private static final String PASSWORD = "123";
  
  public AuthenticatedUserDto admin;
  public AuthenticatedUserDto moderator;
  public AuthenticatedUserDto user;
  
  private @Autowired ContextHolder<Context> contextHolder;
  private @Autowired Environment environment;
  private @Autowired ServiceProvider serviceProvider;
  private @Autowired DtoFactory dtoFactory;
  private @Autowired UserRepository<User> userRepo;
  private @Value("${spring.jpa.hibernate.ddl-auto:''}") String schemaExport;
  private @PersistenceContext EntityManager entityManager;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (environment.acceptsProfiles("test") || schemaExport.startsWith("create")) {
      log.info("Start generating sample data");
      createMarkets();
      
      // TODO don't load all the sample data into test env.
      // atm tests rely on this data, that has to be fixed first
      if (environment.acceptsProfiles("dev", "test")) {
        createUsers();
        createBrands();
        createCategories();
        createProducts();
        createReviews();
        createReviewComments();
        createVotes();
      }
      log.info("Finished generating sample data");
    }
  }
  
  private void createMarkets() {
    // TODO when there is a default market in all environments this can be removed. 
    // tests that rely on multiple markets should create their own anyways.
    // see https://github.com/xinra-it/review-community/issues/27
    
    MarketDto market = dtoFactory.createDto(MarketDto.class);
    market.setName("Deutschland");
    market.setSlug("de");
    serviceProvider.getService(MarketService.class).createMarket(market);
    
    market = dtoFactory.createDto(MarketDto.class);
    market.setName("United States");
    market.setSlug("us");
    serviceProvider.getService(MarketService.class).createMarket(market);
  }
  
  private void createUsers() {
    UserService userService = serviceProvider.getService(UserService.class);
    
    // create admin
    userService.createUserWithPassword(USERNAME_ADMIN, null, PASSWORD);
    userService.addRole(USERNAME_ADMIN, Role.ADMIN);
    admin = AuthenticationProviderImpl
        .getAuthenticatedUserDto(userRepo.findByNameFetchRoles(USERNAME_ADMIN), dtoFactory);
    
    // create moderator
    userService.createUserWithPassword(USERNAME_MODERATOR, null, PASSWORD);
    userService.addRole(USERNAME_MODERATOR, Role.MODERATOR);
    moderator = AuthenticationProviderImpl
        .getAuthenticatedUserDto(userRepo.findByNameFetchRoles(USERNAME_MODERATOR), dtoFactory);
    
    // create normal user
    userService.createUserWithPassword(USERNAME_USER, null, PASSWORD);
    user = AuthenticationProviderImpl
        .getAuthenticatedUserDto(userRepo.findByNameFetchRoles(USERNAME_USER), dtoFactory);
  }

  private void createCategories() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));

    CategoryService categoryService = serviceProvider.getService(CategoryService.class);

    CreateCategoryDto createCategoryDto1 = dtoFactory.createDto(CreateCategoryDto.class);
    createCategoryDto1.setName("Lebensmittel");
    createCategoryDto1.setParentSerial(0);
    categoryService.createCategory(createCategoryDto1);

    CreateCategoryDto createCategoryDto2 = dtoFactory.createDto(CreateCategoryDto.class);
    createCategoryDto2.setName("Süßigkeiten");
    createCategoryDto2.setParentSerial(1);
    categoryService.createCategory(createCategoryDto2);

    CreateCategoryDto createCategoryDto3 = dtoFactory.createDto(CreateCategoryDto.class);
    createCategoryDto3.setName("Fruchtgummi");
    createCategoryDto3.setParentSerial(2);
    categoryService.createCategory(createCategoryDto3);

    CreateCategoryDto createCategoryDto4 = dtoFactory.createDto(CreateCategoryDto.class);
    createCategoryDto4.setName("Getränke");
    createCategoryDto4.setParentSerial(1);
    categoryService.createCategory(createCategoryDto4);

    CreateCategoryDto createCategoryDto5 = dtoFactory.createDto(CreateCategoryDto.class);
    createCategoryDto5.setName("Softdrinks");
    createCategoryDto5.setParentSerial(4);
    categoryService.createCategory(createCategoryDto5);

    contextHolder.clearMock();
  }

  private void createBrands() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));

    BrandService brandService = serviceProvider.getService(BrandService.class);

    CreateBrandDto createBrandDto1 = dtoFactory.createDto(CreateBrandDto.class);
    createBrandDto1.setName("Haribo");
    brandService.createBrand(createBrandDto1);

    CreateBrandDto createBrandDto2 = dtoFactory.createDto(CreateBrandDto.class);
    createBrandDto2.setName("Coca Cola");
    brandService.createBrand(createBrandDto2);

    contextHolder.clearMock();
  }

  private void createProducts() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));

    CreateProductDto createProductDto1 = dtoFactory.createDto(CreateProductDto.class);
    createProductDto1.setName("Color-Rado");
    createProductDto1.setDescription("They're sweet.");
    createProductDto1.setBrandSerial(1);
    createProductDto1.setCategorySerial(3);
    createProductDto1.setBarcode("dummy");

    CreateProductDto createProductDto2 = dtoFactory.createDto(CreateProductDto.class);
    createProductDto2.setName("Goldbären");
    createProductDto2.setDescription("They're golden.");
    createProductDto2.setBrandSerial(1);
    createProductDto2.setCategorySerial(3);
    createProductDto2.setBarcode("temp");

    CreateProductDto createProductDto3 = dtoFactory.createDto(CreateProductDto.class);
    createProductDto3.setName("PUBG");
    createProductDto3.setDescription("Battle Royal");
    createProductDto3.setBrandSerial(2);
    createProductDto3.setCategorySerial(5);
    createProductDto3.setBarcode("battleroyal");

    CreateProductDto createProductDto4 = dtoFactory.createDto(CreateProductDto.class);
    createProductDto4.setName("Coca Cola Zero");
    createProductDto4.setDescription("Echter Geschmack - Zero Zucker");
    createProductDto4.setBrandSerial(2);
    createProductDto4.setCategorySerial(5);
    createProductDto4.setBarcode("qwerty");

    CreateProductDto createProductDto5 = dtoFactory.createDto(CreateProductDto.class);
    createProductDto5.setName("Coca Cola ");
    createProductDto5.setDescription("Timeless taste");
    createProductDto5.setBrandSerial(2);
    createProductDto5.setCategorySerial(5);
    createProductDto5.setBarcode("checkstyle");

    ProductService productService = serviceProvider.getService(ProductService.class);
    productService.createProduct(createProductDto1);
    productService.createProduct(createProductDto2);
    productService.createProduct(createProductDto3);
    productService.createProduct(createProductDto4);
    productService.createProduct(createProductDto5);

    contextHolder.clearMock();
  }

  private void createReviews() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));

    contextHolder.get().setAuthenticatedUser(admin);
    CreateReviewDto createReviewDto1 = dtoFactory.createDto(CreateReviewDto.class);
    createReviewDto1.setTitle("My Review on PUBG");
    createReviewDto1.setText("FPP ftw!");
    createReviewDto1.setRating(5);
    
    ReviewService reviewService = serviceProvider.getService(ReviewService.class);
    reviewService.createReview(createReviewDto1, 3);

    CreateReviewDto createReviewDto4 = dtoFactory.createDto(CreateReviewDto.class);
    createReviewDto4.setTitle("My Review on Coca Cola Zero");
    createReviewDto4.setText("Tastes really good!");
    createReviewDto4.setRating(5);
    reviewService.createReview(createReviewDto4, 4);

    contextHolder.get().setAuthenticatedUser(moderator);
    CreateReviewDto createReviewDto2 = dtoFactory.createDto(CreateReviewDto.class);
    createReviewDto2.setTitle("My Review on PUBG");
    createReviewDto2.setText("E-Sports ready");
    createReviewDto2.setRating(3);
    reviewService.createReview(createReviewDto2, 3);

    contextHolder.get().setAuthenticatedUser(user);
    CreateReviewDto createReviewDto3 = dtoFactory.createDto(CreateReviewDto.class);
    createReviewDto3.setTitle("My Review on PUBG");
    createReviewDto3.setText("Early Access");
    createReviewDto3.setRating(4);
    reviewService.createReview(createReviewDto3, 3);


    contextHolder.clearMock();
  }

  private void createVotes() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));
    contextHolder.get().setAuthenticatedUser(admin);

    ReviewService reviewService = serviceProvider.getService(ReviewService.class);

    ReviewVoteDto reviewVoteDto1 = dtoFactory.createDto(ReviewVoteDto.class);
    reviewVoteDto1.setUpvote(true);
    reviewService.vote(reviewVoteDto1, 1, 3);

    contextHolder.get().setAuthenticatedUser(user);
    ReviewVoteDto reviewVoteDto3 = dtoFactory.createDto(ReviewVoteDto.class);
    reviewVoteDto3.setUpvote(false);
    reviewService.vote(reviewVoteDto3, 1, 3);

    ReviewVoteDto reviewVoteDto4 = dtoFactory.createDto(ReviewVoteDto.class);
    reviewVoteDto4.setUpvote(true);
    reviewService.vote(reviewVoteDto4, 2 ,3);

    contextHolder.get().setAuthenticatedUser(moderator);
    ReviewVoteDto reviewVoteDto2 = dtoFactory.createDto(ReviewVoteDto.class);
    reviewVoteDto2.setUpvote(true);
    reviewService.vote(reviewVoteDto2, 1, 3);

    ReviewVoteDto reviewVoteDto5 = dtoFactory.createDto(ReviewVoteDto.class);
    reviewVoteDto5.setUpvote(false);
    reviewService.vote(reviewVoteDto5, 2, 3);
    
    contextHolder.clearMock();
  }

  private void createReviewComments() {
    contextHolder.mock().setMarket(serviceProvider.getService(MarketService.class).getBySlug("de"));
    contextHolder.get().setAuthenticatedUser(admin);

    ReviewService reviewService = serviceProvider.getService(ReviewService.class);

    CreateReviewCommentDto createReviewCommentDto =
            dtoFactory.createDto(CreateReviewCommentDto.class);
    createReviewCommentDto.setText("Thanks for your review.");
    reviewService.createReviewComment(createReviewCommentDto, 1, 3);

    CreateReviewCommentDto createReviewCommentDto2 =
            dtoFactory.createDto(CreateReviewCommentDto.class);
    createReviewCommentDto2.setText("Thanks for your review.");
    reviewService.createReviewComment(createReviewCommentDto2, 1, 3);

    CreateReviewCommentDto createReviewCommentDto3 =
            dtoFactory.createDto(CreateReviewCommentDto.class);
    createReviewCommentDto3.setText("Thanks for your review.");
    reviewService.createReviewComment(createReviewCommentDto3, 2, 3);

    contextHolder.clearMock();
  }

  @Override
  public int getOrder() {
    return LOWEST_PRECEDENCE;
  }
}
