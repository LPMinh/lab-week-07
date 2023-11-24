package com.minh.labweek07.frontend;

import com.minh.labweek07.backend.enums.Color;
import com.minh.labweek07.backend.enums.Size;
import com.minh.labweek07.backend.models.*;
import com.minh.labweek07.backend.repository.CustomerRepository;
import com.minh.labweek07.backend.repository.OrderRepository;
import com.minh.labweek07.backend.repository.ProductDetailRepository;
import com.minh.labweek07.backend.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class CartController {
    @Autowired private OrderRepository   orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductDetailRepository productDetailRepository;
    @Autowired private CustomerRepository customerRepository;
    @PostMapping("/add-to-cart")
    public String addToCart(HttpSession session, Model model, @RequestParam("id") long id, @RequestParam("color") int color, @RequestParam("size") int size, @RequestParam("quantity") int quantity, @RequestParam("price") double price,@RequestParam("name") String name,@RequestParam("image") String image){

        List<Cart> carts=(List<Cart>) session.getAttribute("cart");
        if(carts==null){
            carts=new ArrayList<>();
            Color colorE=Color.getColorFromInt(color);
            Size sizeE=Size.getSizeFromInt(size);
            Cart cartItem= new Cart(id,name,image,price,colorE,sizeE,quantity)  ;
            carts.add(cartItem);
        }
        else{
           boolean isExist=false;
              for(Cart cart:carts){
                if(cart.getId()==id&&cart.getColor().ordinal()==color&&cart.getSize().ordinal()==size){
                     cart.setQuantity(cart.getQuantity()+quantity);
                     isExist=true;
                     break;
                }
              }
              if(!isExist){
                  Color colorE=Color.getColorFromInt(color);
                  Size sizeE=Size.getSizeFromInt(size);
                  Cart cartItem= new Cart(id,name,image,price,colorE,sizeE,quantity)  ;
                  carts.add(cartItem);
              }


        }
        model.addAttribute("cart",carts);
        session.setAttribute("cart",carts);
        return "user/cart";
    }
    @GetMapping("/cart")
    public String cart(HttpSession session,Model model){
        List<Cart> carts=(List<Cart>) session.getAttribute("cart");
        if (carts==null){
            carts=new ArrayList<>();
            session.setAttribute("cart",carts);
        }
        model.addAttribute("cart",carts);
        double total=0;
        for(Cart cart:carts){
            total+=cart.getPrice()*cart.getQuantity();
        }
        model.addAttribute("total",total);
        return "user/cart";
    }
    @PostMapping("/update-cart")
    @ResponseBody
    public String updateCart(HttpSession session, Model model,
                             @RequestParam("id") long id,
                             @RequestParam("color") int color,
                             @RequestParam("size") int size,
                             @RequestParam("quantity") int quantity) {
        List<Cart> carts = (List<Cart>) session.getAttribute("cart");
        for (Cart cart : carts) {
            if (cart.getId() == id && cart.getColor().ordinal() == color && cart.getSize().ordinal() == size) {
                cart.setQuantity(quantity);
                break;
            }
        }

        double total = 0;
        for (Cart cart : carts) {
            total += cart.getPrice() * cart.getQuantity();
        }

        session.setAttribute("cart", carts);

        // You can return a response message if needed
        return "Cart updated successfully";
    }
    @GetMapping("/check-out")
    public String checkOut(HttpSession session,Model model){
        List<Cart> carts=(List<Cart>) session.getAttribute("cart");
        Set<OrderDetail> orderDetails=new HashSet<>();
        Order order=new Order();
        System.out.println("size:"+carts.size());
        if (carts==null){
            model.addAttribute("message","Chưa có sản phẩm trong giỏ hàng");
            return "user/cart";
        }else{
            carts.forEach(n->{
                OrderDetail orderDetail=new OrderDetail();
                orderDetail.setPrice(n.getPrice());
                orderDetail.setQuantity(n.getQuantity());
                int color=n.getColor().ordinal();
                int size=n.getSize().ordinal();
                Product product=productRepository.findById(n.getId()).get();
                ProductDetail productDetail=productDetailRepository.findProductDetailsByColorAndProductAndSize(n.getColor(),product,n.getSize());
                System.out.println("productDetail:"+productDetail.getProduct().getProductId());
                orderDetail.setProductDetail(productDetail);
                orderDetail.setOrder(order);
                orderDetails.add(orderDetail);
            });
        }
        Account account=(Account) session.getAttribute("acc");
        System.out.println("account:"+account.getAccountID());
        Customer customer=customerRepository.findCustomerByAccountAccountID(account.getAccountID()).get(0);
        System.out.println("customer:"+customer.getCustId());
        order.setCustomer(customer);
        order.setOrderDetails(orderDetails);
        order.setOrderDate(Date.valueOf(LocalDate.now()));
        if( orderRepository.save(order)!=null){
            session.removeAttribute("cart");
            model.addAttribute("message","Đặt hàng thành công");
            return "user/cart";
        }else {
            model.addAttribute("message","Đặt hàng thất bại");
            return "user/cart";
        }


    }

}
