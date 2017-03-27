//
//  SpinnerPlugin.m
//
//

#import "SpinnerPlugin.h"
#import <objc/runtime.h>

@interface SpinnerPlugin()

@end

@implementation SpinnerPlugin

MBProgressHUD *progressIndicator;
NSTimer *scheduleTimer = nil;
CDVInvokedUrlCommand *scheduleCommand;


#pragma mark - Cordova commands
- (void)schedule:(CDVInvokedUrlCommand *)command{
    if ([command.arguments count] != 3) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"A labelText, dimBackground and timeout are required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    if(scheduleTimer != nil){
        [scheduleTimer invalidate];
    }
    scheduleCommand = command;
    double timeout = [[command argumentAtIndex:2] longValue];
    timeout = timeout / 1000;
    scheduleTimer =[NSTimer timerWithTimeInterval: timeout target:self selector:@selector(timerFired:) userInfo:nil  repeats:FALSE ] ;
    [[NSRunLoop currentRunLoop] addTimer:scheduleTimer forMode:UITrackingRunLoopMode];


    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];


}

-(void)cancel:(CDVInvokedUrlCommand *)command{

    if(scheduleTimer != nil){
        [scheduleTimer invalidate];
    }
    scheduleTimer = nil;
    [self activityStop:command];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}
- (void)timerFired:(NSTimer *)timer{
    [self activityStart:scheduleCommand];
    [timer invalidate];
    timer = nil;

}
- (void)activityStart:(CDVInvokedUrlCommand *)command {

    // Ensure we have the correct number of arguments.
    if ([command.arguments count] < 2) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"A labelText and dimBackground are required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    // Obtain the arguments.
    NSString* labelText = [command.arguments objectAtIndex:0];
    BOOL dimBackground = [[command argumentAtIndex:1] boolValue];

    // Ensure any previous dialogs are closed first.
    if (progressIndicator) {
        [progressIndicator hide:YES];
        progressIndicator = nil;
    }

    progressIndicator = nil;
    progressIndicator = [MBProgressHUD showHUDAddedTo:self.webView.superview animated:YES];
    progressIndicator.mode = MBProgressHUDModeIndeterminate;
    progressIndicator.dimBackground = dimBackground;

    // If an optional label value was provided, use it.
    if (labelText) {
        progressIndicator.labelText = labelText;
    }

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)activityStop:(CDVInvokedUrlCommand *)command {

    // If the progress indicator wasn't visible, there is nothing to do.
    if (!progressIndicator) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    // Hide the progress indicator and nil out the reference.
    [progressIndicator hide:YES];
    progressIndicator = nil;

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
