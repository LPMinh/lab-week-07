package com.minh.labweek07.frontend;

import com.minh.labweek07.backend.enums.Color;
import com.minh.labweek07.backend.models.Cart;
import com.minh.labweek07.backend.models.User;
import com.minh.labweek07.backend.models.Product;
import com.minh.labweek07.backend.repository.ProductDetailRepository;
import com.minh.labweek07.backend.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @GetMapping(value = {"/home","/"})
    public String showHomePage(Model model, HttpSession session){
            List<Cart> carts=(List<Cart>) session.getAttribute("cart");
            model.addAttribute("sizeCart",carts==null?0:carts.size());
             User acc=(User) session.getAttribute("acc");
             List<Product> products=productRepository.findAll().stream().limit(10).toList();
             System.out.println(products.size());
                model.addAttribute("products", products);
             return "user/home";
    }
    @GetMapping(value = {"/product-detail"})
    public String showProductDetail(@RequestParam("id") long id, Model model,HttpSession session){

        Product product=productRepository.findById(id).get();
        model.addAttribute("product", product);
        List<Color> colors=productDetailRepository.findDistinctByProduct(id);

        model.addAttribute("colors", colors);
        return "user/product-detail";
    }

}
