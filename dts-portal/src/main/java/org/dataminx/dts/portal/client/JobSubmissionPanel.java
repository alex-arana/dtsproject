/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.portal.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gerson Galang
 */
public class JobSubmissionPanel extends ContentPanel {

    private Window fileServerToConnectWindow;
    private TreePanel<BaseTreeModel> sourceTreePanel;
    private TreePanel<BaseTreeModel> targetTreePanel;
    private AddFileServerToConnectSelectionListener addFileServerListener;

    private static final int CENTER_PANEL_WIDTH = 700;

    private DNDListener treeDragAndDropListener = new DNDListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void dragStart(DNDEvent e) {
            TreePanel tree = ((TreePanel) e.getComponent());
            ModelData sel = tree.getSelectionModel().getSelectedItem();
            //if (sel != null && tree.getStore().getParent(sel) == null) {
            //    e.setCancelled(true);
            //    e.getStatus().setStatus(false);
            //    return;
            //}
            super.dragStart(e);
        }

        @Override
        public void dragDrop(DNDEvent e) {
            sourceTreePanel.disable();
            targetTreePanel.disable();

            // TODO: enable the add another transfer option
        }
    };

    public JobSubmissionPanel() {
        super();
        addFileServerListener = new AddFileServerToConnectSelectionListener();

        setHeading("Job Submission Details");

        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setWidth(CENTER_PANEL_WIDTH);
        layoutContainer.setHeight(270);

        layoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));

        layoutContainer.add(getRenderedSourceTreePanel(), new RowData(.5, 1, new Margins(0, 2, 0, 0)));
        layoutContainer.add(getRenderedTargetTreePanel(), new RowData(.5, 1, new Margins(0, 0, 0, 2)));

        add(layoutContainer);

        //add(subJobContainerPanel, new FlowData(5, 0, 0, 0));


        layoutContainer = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.END);
        layoutContainer.setHeight(50);
        layoutContainer.setLayout(layout);
        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 0, 0, 0));
        layoutContainer.add(new Button("Submit Job"), layoutData);
        add(layoutContainer);
    }

    private LayoutContainer getRenderedSourceTreePanel() {
        final LayoutContainer sourceTreePanelContainer = new LayoutContainer();
        Button button = new Button("Add Source");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                System.out.println("Add source button pressed");
                showFileServerToConnectForm(sourceTreePanelContainer, true);
            }
        });
        sourceTreePanelContainer.add(button, new MarginData(5));
        return sourceTreePanelContainer;
    }

    private LayoutContainer getRenderedTargetTreePanel() {
        final LayoutContainer targetTreePanelContainer = new LayoutContainer();
        Button button = new Button("Add Target");
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                System.out.println("Add target button pressed");
                showFileServerToConnectForm(targetTreePanelContainer, false);
            }
        });
        targetTreePanelContainer.add(button, new MarginData(5));
        return targetTreePanelContainer;
    }

    private void showFileServerToConnectForm(final LayoutContainer treePanelContainer, final boolean isSource) {
        System.out.println("showFileServerToConnectForm() isSource: " + isSource);
        final FormPanel formPanel = new FormPanel();
        if (fileServerToConnectWindow == null) {
            fileServerToConnectWindow = new Window();

            SimpleComboBox<String> simpleComboBox = new SimpleComboBox<String>();
            simpleComboBox.setFieldLabel("Protocol");
            simpleComboBox.setEditable(false);
            simpleComboBox.setSimpleValue("ftp");
            simpleComboBox.add("file");
            simpleComboBox.add("ftp");
            simpleComboBox.add("gsiftp");
            formPanel.add(simpleComboBox);

            TextField<String> hostField = new TextField<String>();
            hostField.setFieldLabel("Host");
            //hostField.setAllowBlank(false);
            formPanel.add(hostField);

            TextField<String> directoryField = new TextField<String>();
            directoryField.setFieldLabel("Directory");
            formPanel.add(directoryField);

            TextField<String> usernameField = new TextField<String>();
            usernameField.setFieldLabel("Username");
            formPanel.add(usernameField);

            TextField<String> passwordField = new TextField<String>();
            passwordField.setFieldLabel("Password");
            passwordField.setPassword(true);
            formPanel.add(passwordField);

            Button button = new Button("Connect");
            System.out.println("isSource outside of anonym: " + isSource);
            addFileServerListener.setPreRequisites(formPanel, treePanelContainer, isSource);
            button.addSelectionListener(addFileServerListener);
            formPanel.addButton(button);
            //formPanel.layout();

            fileServerToConnectWindow.setFrame(true);
            fileServerToConnectWindow.setWidth(400);
            fileServerToConnectWindow.setHeight(250);
            fileServerToConnectWindow.setAutoHeight(true);
            fileServerToConnectWindow.add(formPanel);
            fileServerToConnectWindow.show();
        }
        else {
            addFileServerListener.setPreRequisites(formPanel, treePanelContainer, isSource);
            fileServerToConnectWindow.show();
        }

    }

    private void renderTreePanel(LayoutContainer treePanelContainer, boolean isSource) {
        System.out.println("renderTreePanel() isSource: " + isSource);

        if (isSource) {
            if (sourceTreePanel != null) {
                System.out.println("removing treePanel and isSource: " + isSource);
                treePanelContainer.remove(sourceTreePanel);
            }
            sourceTreePanel = getPopulatedTreePanel();
            treePanelContainer.add(sourceTreePanel, new MarginData(new Margins(5, 5, 5, 5)));

            TreePanelDragSource source = new TreePanelDragSource(sourceTreePanel);
            source.addDNDListener(treeDragAndDropListener);
        }
        else {
            targetTreePanel = getPopulatedTreePanel();

            treePanelContainer.add(targetTreePanel, new MarginData(new Margins(5, 5, 5, 5)));
            TreePanelDropTarget target = new TreePanelDropTarget(targetTreePanel);
            target.addDNDListener(treeDragAndDropListener);
        }
        treePanelContainer.layout();
    }

    private TreePanel<BaseTreeModel> getPopulatedTreePanel() {
        TreePanel<BaseTreeModel> treePanel = new TreePanel<BaseTreeModel>(getDummyTreeData());
        treePanel.setBorders(true);
        treePanel.setSize(150, 200);
        treePanel.setDisplayProperty("name");
        treePanel.setAutoWidth(true);
        //treePanel.getStyle().setLeafIcon(IconHelper.createStyle("x-ftree2-icon"));
        return treePanel;
    }

    private TreeStore<BaseTreeModel> getDummyTreeData() {
        TreeStore<BaseTreeModel> treeStore = new TreeStore<BaseTreeModel>();
        List<BaseTreeModel> baseTreeModelList = new ArrayList<BaseTreeModel>();

        BaseTreeModel btmParent = new BaseTreeModel();
        btmParent.set("name", "Directory1");
        BaseTreeModel btm = new BaseTreeModel();
        btm.set("name", "File1");
        btmParent.add(btm);
        btm = new BaseTreeModel();
        btm.set("name", "File2");
        btmParent.add(btm);

        baseTreeModelList.add(btmParent);

        btmParent = new BaseTreeModel();
        btmParent.set("name", "Directory2");
        BaseTreeModel btmChildParent = new BaseTreeModel();
        btmChildParent.set("name", "Directory3");
        btmParent.add(btmChildParent);
        btm = new BaseTreeModel();
        btm.set("name", "File3");
        btmChildParent.add(btm);

        baseTreeModelList.add(btmParent);

        treeStore.add(baseTreeModelList, true);
        return treeStore;
    }

    private class AddFileServerToConnectSelectionListener extends SelectionListener<ButtonEvent> {

        private FormPanel formPanel;
        private boolean isSource;
        private LayoutContainer treePanelContainer;

        private void setPreRequisites(FormPanel formPanel, LayoutContainer treePanelContainer, boolean isSource) {
            this.formPanel = formPanel;
            this.treePanelContainer = treePanelContainer;
            this.isSource = isSource;
        }

        @Override
        public void componentSelected(ButtonEvent ce) {
            if (formPanel.isValid()) {
                fileServerToConnectWindow.hide();
                System.out.println("isSource before renderTreePanel: " + isSource);
                renderTreePanel(treePanelContainer, isSource);
            }
        }
    }

}
