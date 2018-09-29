/*
 * Copyright (c) 2013-2018. BIN.CHEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.asosat.thorntail.demo.domain;

import static org.asosat.kernel.resource.GlobalMessageCodes.ERR_PARAM;
import static org.asosat.kernel.resource.GlobalMessageCodes.ERR_SYS;
import static org.asosat.kernel.util.MyObjUtils.isEquals;
import static org.asosat.kernel.util.Preconditions.requireFalse;
import static org.asosat.kernel.util.Preconditions.requireNotBlank;
import static org.asosat.kernel.util.Preconditions.requireTrue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.asosat.domain.aggregate.AbstractDefaultGenericAggregate;


/**
 * asosat-thorntail-example
 *
 * @author bingo 下午7:30:41
 *
 */
@Entity
@Table(name = "AT_ORD")
public class Order extends AbstractDefaultGenericAggregate<Map<String, Object>, Order> {

  private static final long serialVersionUID = -3863730848853350242L;

  @Column
  private Instant date = Instant.now();

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column
  private String number;

  @Column
  private String seller;

  @Column
  private String buyer;

  @Column
  private BigDecimal totalAmount;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "AT_ORD_ITM", joinColumns = {@JoinColumn(name = "orderId")})
  @OrderColumn(name = "seq")
  private List<OrderItem> items = new ArrayList<>();

  @Column
  private boolean confirmed;

  @Column
  private Instant confirmedTime;

  /**
   *
   * @param number
   * @param seller
   * @param buyer
   */
  public Order(String number, String seller, String buyer) {
    super();
    this.number = number;
    this.seller = requireNotBlank(seller, ERR_PARAM);
    this.buyer = requireNotBlank(buyer, ERR_PARAM);
  }

  protected Order() {}

  public void addItem(String product, BigDecimal qty, BigDecimal unitPrice) {
    Optional<OrderItem> itemExistOp =
        this.items.stream().filter(i -> isEquals(i.getProduct(), product)).findFirst();
    if (itemExistOp.isPresent()) {
      requireTrue(itemExistOp.get(), (i) -> isEquals(i.getUnitPrice(), unitPrice), ERR_PARAM)
          .updateQty(qty);
    } else {
      this.items.add(new OrderItem(product, qty, unitPrice));
    }
    this.calTotalAmount();
  }

  public void confirm(Map<String, Object> param, Consumer<Order> preConfirmHandler) {
    requireFalse(this.isConfirmed(), ERR_SYS);
    preConfirmHandler.accept(this);
    this.confirmed = true;
    this.confirmedTime = Instant.now();
    if (!this.isEnabled()) {
      this.enable(true);
    }
    this.arise(new OrderConfirmedEvent(this));
  }

  /**
   *
   * @return the buyer
   */
  public String getBuyer() {
    return this.buyer;
  }

  /**
   * @return the confirmedTime
   */
  public Instant getConfirmedTime() {
    return this.confirmedTime;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  /**
   *
   * @return the items
   */
  public List<OrderItem> getItems() {
    return Collections.unmodifiableList(this.items);
  }

  /**
   *
   * @return the number
   */
  public String getNumber() {
    return this.number;
  }

  /**
   *
   * @return the seller
   */
  public String getSeller() {
    return this.seller;
  }

  /**
   *
   * @return the totalAmount
   */
  public BigDecimal getTotalAmount() {
    return this.totalAmount;
  }

  /**
   *
   * @return the confirmed
   */
  public boolean isConfirmed() {
    return this.confirmed;
  }

  public boolean removeItemIf(Predicate<OrderItem> filter) {
    boolean removed = this.items.removeIf(filter);
    if (removed) {
      this.calTotalAmount();
    }
    return removed;
  }

  void calTotalAmount() {
    this.totalAmount =
        this.items.stream().map(OrderItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
