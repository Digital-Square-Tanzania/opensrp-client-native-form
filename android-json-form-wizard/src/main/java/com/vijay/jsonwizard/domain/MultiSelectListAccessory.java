package com.vijay.jsonwizard.domain;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.vijay.jsonwizard.adapter.MultiSelectListAdapter;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;

import org.json.JSONObject;

import java.util.List;

public class MultiSelectListAccessory {
    private MultiSelectListSelectedAdapter selectedAdapter;
    private MultiSelectListAdapter listAdapter;
    private AlertDialog alertDialog;
    private Button actionButton;
    private View actionSeparator;
    private List<MultiSelectItem> selectedItemList;
    private List<MultiSelectItem> itemList;
    private JSONObject formAttributes;

    public MultiSelectListAccessory(MultiSelectListSelectedAdapter selectedAdapter,
                                    MultiSelectListAdapter listAdapter, AlertDialog alertDialog,
                                    List<MultiSelectItem> selectedItemList, List<MultiSelectItem> itemList) {
        this.selectedAdapter = selectedAdapter;
        this.listAdapter = listAdapter;
        this.alertDialog = alertDialog;
        this.selectedItemList = selectedItemList;
        this.itemList = itemList;
        this.formAttributes = new JSONObject();
    }

    public MultiSelectListSelectedAdapter getSelectedAdapter() {
        return selectedAdapter;
    }

    public void setSelectedAdapter(MultiSelectListSelectedAdapter selectedAdapter) {
        this.selectedAdapter = selectedAdapter;
    }

    public MultiSelectListAdapter getListAdapter() {
        return listAdapter;
    }

    public void setListAdapter(MultiSelectListAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    public void setAlertDialog(AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void setActionButton(Button actionButton) {
        this.actionButton = actionButton;
    }

    public View getActionSeparator() {
        return actionSeparator;
    }

    public void setActionSeparator(View actionSeparator) {
        this.actionSeparator = actionSeparator;
    }

    public List<MultiSelectItem> getItemList() {
        return itemList;
    }

    public JSONObject getFormAttributes() {
        return formAttributes;
    }

    public void setFormAttributes(JSONObject formAttributes) {
        this.formAttributes = formAttributes;
    }

    public void setItemList(List<MultiSelectItem> itemList) {
        this.itemList = itemList;
    }

    public List<MultiSelectItem> getSelectedItemList() {
        return selectedItemList;
    }
}
