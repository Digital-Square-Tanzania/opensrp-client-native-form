package com.vijay.jsonwizard.widgets;

import android.support.v7.app.AlertDialog;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.adapter.MultiSelectListAdapter;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.domain.MultiSelectListAccessory;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.repository.TestMultiSelectListRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@LooperMode(PAUSED)
@PrepareForTest({ViewUtil.class})
public class MultiSelectListFactoryTest extends FactoryTest {

    private MultiSelectListFactory multiSelectListFactory;

    @Before
    public void setUp() {
        super.setUp();
        multiSelectListFactory = Mockito.spy(new MultiSelectListFactory());
    }

    @Test
    public void prepareSelectedDataShouldReturnEmptyArrayList() {
        multiSelectListFactory.jsonObject = new JSONObject();
        Assert.assertEquals(new ArrayList<>(), multiSelectListFactory.prepareSelectedData());
    }

    @Test
    public void prepareSelectedDataShouldReturnFilledArrayList() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.VALUE,
                new JSONArray("[\n" +
                        "          {\n" +
                        "            \"key\": \"abortion\",\n" +
                        "            \"text\": \"Abortion\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          }\n" +
                        "        ]"));

        Whitebox.setInternalState(multiSelectListFactory, "jsonObject", jsonObject);

        Assert.assertEquals(1, multiSelectListFactory.prepareSelectedData().size());
    }

    @Test
    public void updateSelectedData() throws Exception {

        HashMap<String, MultiSelectListAccessory> accessoryHashMap = new HashMap<>();
        Whitebox.setInternalState(multiSelectListFactory, "multiSelectListAccessoryHashMap", accessoryHashMap);
        MultiSelectListSelectedAdapter multiSelectListAdapter = new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), multiSelectListFactory);

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                multiSelectListAdapter,
                new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        multiSelectListFactory.currentAdapterKey = "test";

        Whitebox.invokeMethod(multiSelectListFactory, "updateMultiSelectListAccessoryHashMap", multiSelectListAccessory);
        try {
            multiSelectListFactory.updateSelectedData(new MultiSelectItem(), true);
        } catch (NullPointerException e) {
            //this exception catches call on notifyDataSetChanged since no recylclerview has been attached to the adapter;
        }
        Assert.assertEquals(1, multiSelectListFactory.getMultiSelectListSelectedAdapter().getData().size());

    }

    @Test
    public void updateListData() throws Exception {
        HashMap<String, MultiSelectListAccessory> accessoryHashMap = new HashMap<>();
        Whitebox.setInternalState(multiSelectListFactory, "multiSelectListAccessoryHashMap", accessoryHashMap);
        MultiSelectListSelectedAdapter multiSelectListAdapter = new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), multiSelectListFactory);

        MultiSelectListAccessory multiSelectListAccessory = new MultiSelectListAccessory(
                multiSelectListAdapter,
                new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<MultiSelectItem>());
        multiSelectListFactory.currentAdapterKey = "test";

        List<MultiSelectItem> multiSelectItems = new ArrayList<>();
        multiSelectItems.add(new MultiSelectItem());
        multiSelectListAccessory.setItemList(multiSelectItems);

        Whitebox.invokeMethod(multiSelectListFactory, "updateMultiSelectListAccessoryHashMap", multiSelectListAccessory);
        try {

            multiSelectListFactory.updateListData(true);
        } catch (NullPointerException e) {
            //this exception catches call on notifyDataSetChanged since no recylclerview has been attached to the adapter;
        }
        Assert.assertEquals(1, multiSelectListFactory.getMultiSelectListAdapter().getData().size());
    }

    @Test
    public void testLoadListItemsWithExternalSource() throws JSONException {
        String strJsonObject =
                "{\n" +
                        "        \"source\": \"csv\",\n" +
                        "        \"repositoryClass\": \"com.vijay.jsonwizard.repository.TestMultiSelectListRepository\"" +
                        "}";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        Whitebox.setInternalState(multiSelectListFactory, "jsonObject", jsonObject);
        List<MultiSelectItem> selectItems = multiSelectListFactory.loadListItems("settings");

        Assert.assertTrue(new TestMultiSelectListRepository().fetchData().containsAll(selectItems));
    }

    @Test
    public void testLoadListItemsWithOptionsAsDataSource() throws JSONException {
        String strJsonObject =
                "{" +
                        "        \"options\": [\n" +
                        "          {\n" +
                        "            \"key\": \"Bbcess\",\n" +
                        "            \"text\": \"BAbcess\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"key\": \"bacterial_meningitis\",\n" +
                        "            \"text\": \"Bacterial Meningitis\",\n" +
                        "            \"property\": {\n" +
                        "              \"presumed-id\": \"er\",\n" +
                        "              \"confirmed-id\": \"er\"\n" +
                        "            }\n" +
                        "          }]"
                        + "}";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        Whitebox.setInternalState(multiSelectListFactory, jsonObject, jsonObject);
        List<MultiSelectItem> selectItems = multiSelectListFactory.loadListItems(null);
        Assert.assertEquals("Bbcess", selectItems.get(0).getKey());
        Assert.assertEquals("BAbcess", selectItems.get(0).getText());

        Assert.assertEquals("bacterial_meningitis", selectItems.get(1).getKey());
        Assert.assertEquals("Bacterial Meningitis", selectItems.get(1).getText());


        Assert.assertEquals("{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}", selectItems.get(0).getValue());
        Assert.assertEquals("{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}", selectItems.get(1).getValue());

    }

    @Test
    public void testPrepareListDataShouldPopulateTheFactoryListItems() throws JSONException, InterruptedException {
        String strJsonObject = "{\"key\":\"user_dummy\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"buttonText\":\"+ Add disease code\",\"sort\":true,\"groupings\":\"[A,B]\",\"dialogTitle\":\"Add disease code\",\"value\":[{\"key\":\"abortion\",\"text\":\"Abortion\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}}],\"searchHint\":\"Type Disease Name\",\"options\":[{\"key\":\"Bbcess\",\"text\":\"BAbcess\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"bacterial_meningitis\",\"text\":\"Bacterial Meningitis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"abortion\",\"text\":\"Abortion\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"bronchitis\",\"text\":\"Bronchitis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}},{\"key\":\"arucellosis\",\"text\":\"arucellosis\",\"property\":{\"presumed-id\":\"er\",\"confirmed-id\":\"er\"}}],\"type\":\"multi_select_list\"}";
        JSONObject jsonObject = new JSONObject(strJsonObject);
        JsonFormFragment jsonFormFragment = Mockito.spy(new JsonFormFragment());
        Mockito.doReturn(jsonFormActivity).when(jsonFormFragment).getJsonApi();
        Mockito.doReturn(jsonFormFragment).when(multiSelectListFactory).getJsonFormFragment();
        Mockito.doReturn(RuntimeEnvironment.application).when(multiSelectListFactory).getContext();
        multiSelectListFactory.jsonObject = jsonObject;
        multiSelectListFactory.currentAdapterKey = "user_dummy";

        MultiSelectListAdapter multiSelectListAdapterSpy = Mockito.spy(new MultiSelectListAdapter(new ArrayList<MultiSelectItem>()));

        HashMap<String, MultiSelectListAccessory> listAccessoryHashMap = new HashMap<>();
        listAccessoryHashMap.put(multiSelectListFactory.currentAdapterKey,
                new MultiSelectListAccessory(Mockito.mock(MultiSelectListSelectedAdapter.class), multiSelectListAdapterSpy, Mockito.mock(AlertDialog.class), new ArrayList<MultiSelectItem>(), new ArrayList<MultiSelectItem>()));

        Mockito.doReturn(listAccessoryHashMap).when(multiSelectListFactory).getMultiSelectListAccessoryHashMap();

        List<MultiSelectItem> multiSelectItems = multiSelectListFactory.prepareListData();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(TIMEOUT);

        Assert.assertTrue(multiSelectItems.isEmpty());

        Assert.assertFalse(listAccessoryHashMap.get(multiSelectListFactory.currentAdapterKey).getItemList().isEmpty());

        Assert.assertFalse(multiSelectListAdapterSpy.getData().isEmpty());

        Assert.assertEquals(7, multiSelectListAdapterSpy.getData().size());

        Mockito.verify(multiSelectListFactory, Mockito.timeout(2)).updateListData(Mockito.eq(true));
    }
}