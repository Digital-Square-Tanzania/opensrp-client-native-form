package com.vijay.jsonwizard.presenters;

import android.widget.LinearLayout;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.utils.Utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by keyman on 04/12/18.
 */
public class JsonWizardFormFragmentPresenter extends JsonFormFragmentPresenter {
    public JsonWizardFormFragmentPresenter(JsonFormFragment formFragment, JsonFormInteractor jsonFormInteractor) {
        super(formFragment, jsonFormInteractor);
    }

    @Override
    public boolean onNextClick(LinearLayout mainViews) {

        validateAndWriteValues();
        checkAndStopCountdownAlarm();

        boolean validateOnSubmit = validateOnSubmit();
        if (validateOnSubmit && getIncorrectlyFormattedFields().isEmpty()) {
            executeRefreshLogicForNextStep();
            return moveToNextWizardStep();
        } else if (isFormValid()) {
            executeRefreshLogicForNextStep();
            return moveToNextWizardStep();
        } else {
            getView().showSnackBar(getView().getContext().getResources()
                    .getString(R.string.json_form_on_next_error_msg));
        }

        return false;
    }


    public void executeRefreshLogicForNextStep() {
        final String nextStep = getFormFragment().getJsonApi().nextStep();
        if (StringUtils.isNotBlank(nextStep)) {
            getmJsonFormInteractor().fetchFormElements(nextStep, getFormFragment(), getFormFragment().getJsonApi().getmJSONObject().optJSONObject(nextStep), getView().getCommonListener(), false);
            getFormFragment().getJsonApi().initializeDependencyMaps();
            cleanDataForNextStep();
            getFormFragment().getJsonApi().invokeRefreshLogic(null, false, null, null, nextStep, true);
            if (!getFormFragment().getJsonApi().isNextStepRelevant()) {
                Utils.checkIfStepHasNoSkipLogic(getFormFragment());
            }
            ((JsonWizardFormFragment) getFormFragment()).skipStepsOnNextPressed(nextStep);
        }
    }

    private void cleanDataForNextStep() {
        getFormFragment().getJsonApi().setNextStepRelevant(false);
    }

    protected boolean moveToNextWizardStep() {
        final String nextStep = getFormFragment().getJsonApi().nextStep();
        if (!"".equals(nextStep)) {
            JsonFormFragment next = JsonWizardFormFragment.getFormFragment(nextStep);
            getView().hideKeyBoard();
            getView().transactThis(next);
        }
        return false;
    }

}
