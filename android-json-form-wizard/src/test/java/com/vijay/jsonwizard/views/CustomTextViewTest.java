package com.vijay.jsonwizard.views;

import android.app.Application;
import android.content.Context;

import com.vijay.jsonwizard.BaseTest;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

public class CustomTextViewTest extends BaseTest {
    private CustomTextView customTextView;
    @Mock
    private Context context;

    @Before
    public void setUp() throws JSONException {
        Application application = Mockito.spy(Application.class);
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(context).when(application).getApplicationContext();
    }

    @Test
    public void testSetText() {
        customTextView=new CustomTextView(RuntimeEnvironment.application.getApplicationContext());
        String text = "<testing>";
        customTextView.setText(text);
        Assert.assertEquals("<testing>",customTextView.getText());
    }
}
