/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.orders.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.IValidable;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * It represents an {@link Order} with its related information. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Order extends BaseEntity implements IOrderLineGroup, IValidable {

    public static Order create() {
        Order order = new Order();
        order.setNewObject(true);
        return order;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public Order() {

    }

    private static Date copy(Date date) {
        return date != null ? new Date(date.getTime()) : date;
    }

    @NotEmpty
    private String name;


    @NotNull
    private Date initDate;

    private Date endDate;

    private String description;

    private String responsible;

    // TODO turn into a many to one relationship when Customer entity is defined
    private String customer;

    @Valid
    private List<OrderElement> orderElements = new ArrayList<OrderElement>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getInitDate() {
        return copy(initDate);
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return copy(endDate);
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isEndDateBeforeStart() {
        return endDate != null && endDate.before(initDate);
    }

    public void add(OrderElement orderElement) {
        getOrderElementsManipulator().add(orderElement);
    }

    private OrderLineGroupManipulator getOrderElementsManipulator() {
        return OrderLineGroupManipulator.createManipulatorForOrder(orderElements);
    }

    public List<OrderElement> getOrderElements() {
        return new ArrayList<OrderElement>(orderElements);
    }

    public void remove(OrderElement orderElement) {
        getOrderElementsManipulator().remove(orderElement);
    }

    public void replace(OrderElement oldOrderElement, OrderElement orderElement) {
        getOrderElementsManipulator().replace(oldOrderElement, orderElement);
    }

    @Override
    public void up(OrderElement orderElement) {
        getOrderElementsManipulator().up(orderElement);
    }

    @Override
    public void down(OrderElement orderElement) {
        getOrderElementsManipulator().down(orderElement);
    }

    @Override
    public void add(int position, OrderElement orderElement) {
        getOrderElementsManipulator().add(position, orderElement);

    }

    public List<TaskElement> getAssociatedTasks() {
        ArrayList<TaskElement> result = new ArrayList<TaskElement>();
        for (OrderElement orderElement : orderElements) {
            result.addAll(orderElement.getTaskElements());
        }
        return result;
    }

    public boolean isSomeTaskElementScheduled() {
        for (OrderElement orderElement : orderElements) {
            if (orderElement.isScheduled())
                return true;
        }
        return false;
    }

    @Override
    public void checkValid() throws ValidationException {
        if (this.isEndDateBeforeStart()) {
            throw new ValidationException("endDate must be after startDate");
        }

        for (OrderElement orderElement : this.getOrderElements()) {
            if (!orderElement.checkAtLeastOneHoursGroup()) {
                throw new ValidationException(
                        "At least one HoursGroup is needed for each OrderElement");
            }
        }
    }

    public List<DayAssignment> getDayAssignments() {
        List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
        for (OrderElement orderElement : getAllOrderElements()) {
            Set<TaskElement> taskElements = orderElement.getTaskElements();
            for (TaskElement taskElement : taskElements) {
                Set<ResourceAllocation<?>> resourceAllocations = taskElement.getResourceAllocations();
                for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
                    dayAssignments.addAll(resourceAllocation.getAssignments());
                }
            }
        }
        return dayAssignments;
    }

    private List<OrderElement> getAllOrderElements() {
        List<OrderElement> result = new ArrayList<OrderElement>(
                this.orderElements);
        for (OrderElement orderElement : this.orderElements) {
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

}
