/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "ConfigureViewController.h"
#import "ViewController.h"

#import <UriBeacon/UriBeacon.h>
#import <MBProgressHUD/MBProgressHUD.h>

@interface ConfigureViewController ()<UITextFieldDelegate>

@end

@implementation ConfigureViewController {
  UITextField *_textField;
  BOOL _connected;
  MBProgressHUD *_hud;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  [[self view] setBackgroundColor:[UIColor whiteColor]];

  UIBarButtonItem *cancelButton =
      [[UIBarButtonItem alloc] initWithTitle:@"Cancel"
                                       style:UIBarButtonItemStylePlain
                                      target:self
                                      action:@selector(_cancel:)];
  [[self navigationItem] setLeftBarButtonItem:cancelButton];

  CGRect bounds = [[self view] bounds];
  UILabel *label = [[UILabel alloc]
      initWithFrame:CGRectMake(10, 74, bounds.size.width - 10, 0)];
  [label setText:@"Enter URI:"];
  [label sizeToFit];
  [[self view] addSubview:label];

  _textField = [[UITextField alloc]
      initWithFrame:CGRectMake(10, CGRectGetMaxY([label frame]) + 10,
                               bounds.size.width - 20, 20)];
  [_textField setPlaceholder:@"https://www.google.com"];
  [_textField setDelegate:self];
  [_textField setKeyboardType:UIKeyboardTypeURL];
  [_textField setAutocapitalizationType:UITextAutocapitalizationTypeNone];
  [_textField setAutocorrectionType:UITextAutocorrectionTypeNo];
  [_textField setReturnKeyType:UIReturnKeyDone];
  [[self view] addSubview:_textField];
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  [_textField becomeFirstResponder];
  [[[self navigationItem] leftBarButtonItem] setEnabled:NO];
  ConfigureViewController *__weak weakSelf = self;
  [[self beacon] connect:^(NSError *error) {
      _connected = YES;
      ConfigureViewController *strongSelf = weakSelf;
      [[[strongSelf navigationItem] leftBarButtonItem] setEnabled:YES];
  }];
}

- (void)viewDidAppear:(BOOL)animated {
  [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
  [super viewWillDisappear:animated];
  [[self beacon] disconnect:^(NSError *error) { _connected = NO; }];
}

- (void)_cancel:(id)sender {
  [self _dismissViewController];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
  NSURL *url = [NSURL URLWithString:[_textField text]];
  if (url == nil) {
    // The string could not be parsed by NSURL.
    UIAlertView *alert = [[UIAlertView alloc]
            initWithTitle:@"The URL is not valid"
                  message:@"Please check you typed it properly."
                 delegate:nil
        cancelButtonTitle:@"OK"
        otherButtonTitles:nil];
    [alert show];
    return YES;
  }
  UBUriBeacon *beaconData =
      [[UBUriBeacon alloc] initWithURI:url txPowerLevel:32];
  if (![beaconData isValid]) {
    // Does the URL fits into a UriBeacon?
    UIAlertView *alert = [[UIAlertView alloc]
            initWithTitle:@"The URL is too long"
                  message:@"You could use a URL shortener to help."
                 delegate:nil
        cancelButtonTitle:@"OK"
        otherButtonTitles:nil];
    [alert show];
    return YES;
  }

  [_textField resignFirstResponder];

  // Show a progress.
  [self _startProgress];

  // Wait for 2 sec before doing anything to let the user see some feedback.
  [self performSelector:@selector(_reallyWriteBeacon)
             withObject:nil
             afterDelay:2.0];

  return YES;
}

- (void)_startProgress {
  [[[self view] window] setUserInteractionEnabled:NO];

  _hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
  [_hud setMode:MBProgressHUDModeIndeterminate];
  [_hud setLabelText:@"Writing"];
}

- (void)_stopProgress {
  [_hud hide:YES];
  _hud = nil;

  [[[self view] window] setUserInteractionEnabled:YES];
}

- (void)_reallyWriteBeacon {
  NSURL *url = [NSURL URLWithString:[_textField text]];
  UBUriBeacon *beaconData =
      [[UBUriBeacon alloc] initWithURI:url txPowerLevel:32];
  ConfigureViewController *__weak weakSelf = self;
  [[self beacon] writeBeacon:beaconData
             completionBlock:^(NSError *error) {
                 ConfigureViewController *strongSelf = weakSelf;
                 [strongSelf _writeDoneWithError:error];
             }];
}

- (void)_writeDoneWithError:(NSError *)error {
  [self _stopProgress];

  if (error == nil) {
    [self _dismissViewController];
    return;
  }

  [_textField becomeFirstResponder];
  UIAlertView *alert = [[UIAlertView alloc]
          initWithTitle:@"An error occurred while writing the beacon"
                message:[error localizedDescription]
               delegate:nil
      cancelButtonTitle:@"OK"
      otherButtonTitles:nil];
  [alert show];
}

- (void)_dismissViewController {
  [_textField resignFirstResponder];
  ConfigureViewController *__weak weakSelf = self;
  [self dismissViewControllerAnimated:YES
                           completion:^{
                               if ([self delegate] != nil) {
                                 ConfigureViewController *strongSelf = weakSelf;
                                 [[strongSelf delegate]
                                     configurationViewControllerDismissed:self];
                               }
                           }];
}

@end
