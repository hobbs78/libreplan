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
package org.navalplanner.web.templates.materials;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialAssignmentTemplate;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.templates.daos.IOrderElementTemplateDAO;
import org.navalplanner.business.templates.entities.OrderElementTemplate;
import org.navalplanner.web.orders.materials.AssignedMaterialsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AssignedMaterialsToOrderElementTemplateModel extends
        AssignedMaterialsModel<OrderElementTemplate, MaterialAssignmentTemplate>
        implements IAssignedMaterialsToOrderElementTemplateModel {

    @Autowired
    private IOrderElementTemplateDAO templateDAO;

    private OrderElementTemplate template;

    @Override
    protected MaterialCategory addAssignment(
            MaterialAssignmentTemplate materialAssignment) {
        template.addMaterialAssignment(materialAssignment);
        return materialAssignment.getMaterial().getCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public void addMaterialAssignment(Material material) {
        MaterialAssignmentTemplate materialAssignmentTemplate = MaterialAssignmentTemplate
                .create(material);
        addMaterialAssignment(materialAssignmentTemplate);
    }

    @Override
    protected void assignAndReattach(OrderElementTemplate element) {
        this.template = element;
        templateDAO.reattach(element);
    }

    @Override
    protected List<MaterialAssignmentTemplate> getAssignments() {
        return new ArrayList<MaterialAssignmentTemplate>(template
                .getMaterialAssignments());
    }

    @Override
    protected Material getMaterial(MaterialAssignmentTemplate assignment) {
        return assignment.getMaterial();
    }

    @Override
    protected BigDecimal getTotalPrice(MaterialAssignmentTemplate each) {
        return each.getTotalPrice();
    }

    @Override
    protected Double getUnits(MaterialAssignmentTemplate assigment) {
        return assigment.getUnits();
    }

    @Override
    protected void initializeMaterialAssigments() {
        initializeMaterialAssigments(getAssignments());
    }

    private void initializeMaterialAssigments(
            Collection<MaterialAssignmentTemplate> materialAssignments) {
        for (MaterialAssignmentTemplate each : materialAssignments) {
            each.getUnits();
            reattachMaterial(each.getMaterial());
            initializeMaterialCategory(each.getMaterial().getCategory());
        }
    }

    @Override
    protected boolean isInitialized() {
        return template != null;
    }

    @Override
    protected MaterialCategory removeAssignment(
            MaterialAssignmentTemplate materialAssignment) {
        template.removeMaterialAssignment(materialAssignment);
        return materialAssignment.getMaterial().getCategory();
    }

    @Override
    public OrderElementTemplate getTemplate() {
        return template;
    }

    @Override
    public void removeMaterialAssignment(
            MaterialAssignmentTemplate materialAssignment) {
        template.removeMaterialAssignment(materialAssignment);
    }

}
