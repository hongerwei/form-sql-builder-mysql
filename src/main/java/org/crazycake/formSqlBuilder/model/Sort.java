package org.crazycake.formSqlBuilder.model;


import org.crazycake.formSqlBuilder.model.enums.Order;



public class Sort {

	private String sort;

	private Order order;

	public Sort(){}
	
	public Sort(String sort, Order order){
		this.sort = sort;
		this.order = order;
	}
	
	public Sort(String sort,String orderStr){
		this.sort = sort;
		Order[] orders = Order.values();
		for (int i = 0; i < orders.length; i++) {
			if(orders[i].getSql().equals(orderStr)){
				this.order = orders[i];
				break;
			}
		}
	}
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
