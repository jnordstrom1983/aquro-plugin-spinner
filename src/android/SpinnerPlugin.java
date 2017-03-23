package net.justin_credible.cordova;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;

import net.justin_credible.cordova.FakeR;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public final class SpinnerPlugin extends CordovaPlugin {

      private FakeR R = null;
      private ProgressDialog spinnerDialog = null;
      private CountDownTimer countDownTimer = null;

      @Override
      public void pluginInitialize() {
          super.pluginInitialize();

          R = new FakeR(this.cordova.getActivity());
      }

      @Override
      public synchronized boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

          if (action == null) {
              return false;
          }

          if(action.equals("schedule")){
              try {
                  this.schedule(args, callbackContext);
              }
              catch (Exception exception) {
                  callbackContext.error("SpinnerPlugin uncaught exception: " + exception.getMessage());
              }

          }

          if(action.equals("cancel")){
              try {
                  this.cancel(callbackContext);
              }
              catch (Exception exception) {
                  callbackContext.error("SpinnerPlugin uncaught exception: " + exception.getMessage());
              }

          }

          if (action.equals("activityStart")) {

              try {
                  this.activityStart(args, callbackContext);
              }
              catch (Exception exception) {
                  callbackContext.error("SpinnerPlugin uncaught exception: " + exception.getMessage());
              }

              return true;
          }
          else if (action.equals("activityStop")) {

              try {
                  this.activityStop(callbackContext);
              }
              catch (Exception exception) {
                  callbackContext.error("SpinnerPlugin uncaught exception: " + exception.getMessage());
              }

              return true;
          }
          else {
              // The given action was not handled above.
              return false;
          }
      }
      private synchronized  void cancel(CallbackContext callbackContext){
          if(countDownTimer != null){
              countDownTimer.cancel();
              countDownTimer = null;
          }
          try {
              this.activityStop(callbackContext);
          }catch (JSONException ex){

          }

          callbackContext.success();

      }
      private synchronized void schedule(final JSONArray args, final CallbackContext callbackContext) throws  JSONException{
          if (args.length() != 3) {
              callbackContext.error("A labelText, dimBackground and a timeout are required");
              return;
          }

          if(countDownTimer != null){
              countDownTimer.cancel();
              countDownTimer = null;
          }

          // Obtain the arguments.
          final long timeOut = args.getLong(2);

          countDownTimer = new CountDownTimer(timeOut, timeOut) {
              @Override
              public void onTick(long l) {

              }

              @Override
              public void onFinish() {
                  try {
                      activityStart(args, null);
                  }catch (JSONException ex){

                  }
              }
          };
          countDownTimer.start();
          callbackContext.success();

      }
      private synchronized void activityStart(final JSONArray args, final CallbackContext callbackContext) throws JSONException {

          // Ensure we have the correct number of arguments.
          if (args.length() < 2) {
              if(callbackContext != null){
                  callbackContext.error("A labelText and dimBackground are required.");
              }

              return;
          }

          // Obtain the arguments.
          final String labelText = args.getString(0);
          final boolean dimBackground = args.getBoolean(1);

          // Ensure any previous dialogs are closed first.
          if (this.spinnerDialog != null) {
              this.spinnerDialog.dismiss();
              this.spinnerDialog = null;
          }

          if (dimBackground == false) {
              // If we aren't dimming the background, we need to use a special theme to hide it.
              int theme = R.getId("style", "SpinnerPluginTransparentDialog");
              this.spinnerDialog = new ProgressDialog(this.cordova.getActivity(), theme);
          }
          else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
              // Use a more modern looking dialog if the platform supports it.
              this.spinnerDialog = new ProgressDialog(this.cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
          }
          else {
              // Fallback to the platform default.
              this.spinnerDialog = new ProgressDialog(this.cordova.getActivity());
          }

          this.spinnerDialog.setMessage(labelText);
          this.spinnerDialog.setIndeterminate(true);
          this.spinnerDialog.setCancelable(false);
          this.spinnerDialog.show();
          if(callbackContext != null){
              callbackContext.success();
          }

      }

      private synchronized void activityStop(final CallbackContext callbackContext) throws JSONException {

          // Hide the dialog and null out the reference.
          if (this.spinnerDialog != null) {
              this.spinnerDialog.dismiss();
              this.spinnerDialog = null;
          }

          callbackContext.success();
      }
}
