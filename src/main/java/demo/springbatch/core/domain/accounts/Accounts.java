package demo.springbatch.core.domain.accounts;

import demo.springbatch.core.domain.orders.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@ToString
@Getter
@NoArgsConstructor
public class Accounts {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @Column(name="order_item")
  private String orderItem;
  private Integer price;
  @Column(name="order_date")
  private Date orderDate;
  @Column(name="account_date")
  private Date accountDate;

  public Accounts(Orders orders){
    this.id = orders.getId();
    this.orderItem = orders.getOrderItem();
    this.price = orders.getPrice();
    this.orderDate = orders.getOrderDate();
    this.accountDate = new Date();
  }
}
