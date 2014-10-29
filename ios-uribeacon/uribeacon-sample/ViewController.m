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

#import "ViewController.h"

#import "SettingsViewController.h"
#import "ConfigureViewController.h"

#import <UriBeacon/UriBeacon.h>

@interface ViewController ()<UITableViewDataSource, UITableViewDelegate,
                             ConfigureViewControllerDelegate>

@end

@implementation ViewController {
  UBUriBeaconScanner *_scanner;
  SettingsViewController *_settingsViewController;
  ConfigureViewController *_configureViewController;
  NSMutableSet *_lastBeaconsIdentifiers;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  _lastBeaconsIdentifiers = [[NSMutableSet alloc] init];

  [[NSNotificationCenter defaultCenter]
      addObserver:self
         selector:@selector(_appDidBecomeActive:)
             name:UIApplicationDidBecomeActiveNotification
           object:[UIApplication sharedApplication]];

  // Start the scanner.
  _scanner = [[UBUriBeaconScanner alloc] init];
  ViewController *__weak weakSelf = self;
  [_scanner startScanningWithUpdateBlock:^{
      ViewController *strongSelf = weakSelf;
      [[strongSelf tableView] reloadData];
      [strongSelf _openConfiguration];
      [strongSelf _showLocalNotification];
  }];

  UIBarButtonItem *settingsButton =
      [[UIBarButtonItem alloc] initWithTitle:@"Settings"
                                       style:UIBarButtonItemStylePlain
                                      target:self
                                      action:@selector(_openSettings:)];
  [[self navigationItem] setRightBarButtonItem:settingsButton];
}

- (void)_appDidBecomeActive:(NSNotification *)notification {
  // Reset beacons seen in the background if the app becomes active.
  [_lastBeaconsIdentifiers removeAllObjects];
}

// Handler for discovery of UriBeacon in the background.
- (void)_showLocalNotification {
  if ([[UIApplication sharedApplication] applicationState] !=
      UIApplicationStateBackground) {
    return;
  }

  NSMutableArray *discoveredBeacon = [NSMutableArray array];
  // We track in recentBeacons only new beacons that haven't been seen before.
  unsigned int recentBeacons = 0;
  for (UBUriBeacon *beacon in [_scanner beacons]) {
    [discoveredBeacon addObject:beacon];
    if (![_lastBeaconsIdentifiers containsObject:[beacon identifier]]) {
      recentBeacons++;
      [_lastBeaconsIdentifiers addObject:[beacon identifier]];
    }
  }

  if (recentBeacons > 0) {
    // Update the local notification, based on the new information we have.
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    [notification setFireDate:[NSDate date]];
    NSString *body = [NSString
        stringWithFormat:NSLocalizedString(@"%u uriBeacons have been seen",
                                           @"Message when the list of beacons "
                                           @"has been updated. %u is the "
                                           @"number of beacons that have been "
                                           @"seen while scanning for them in "
                                           @"the background."),
                         [discoveredBeacon count]];
    [notification setAlertBody:body];
    [[UIApplication sharedApplication]
        presentLocalNotificationNow:notification];
  }
}

- (void)_openConfiguration {
  // Open beacon configuration panel only if settings have been opened.
  // allowsConfiguration will be set to YES when the setting panel is opened.
  //
  // Also, we don't open a new beacon configuration panel if one is already
  // opened.
  if ([self allowsConfiguration] && (_configureViewController == nil)) {
    if ([[_scanner configurableBeacons] count] > 0) {
      _configureViewController =
          [[ConfigureViewController alloc] initWithNibName:nil bundle:nil];
      [_configureViewController
          setBeacon:[[_scanner configurableBeacons] objectAtIndex:0]];
      [_configureViewController setDelegate:self];
      UINavigationController *configureNavigationController =
          [[UINavigationController alloc]
              initWithRootViewController:_configureViewController];
      [_settingsViewController
          presentViewController:configureNavigationController
                       animated:YES
                     completion:nil];
    }
  }
}

- (void)configurationViewControllerDismissed:
            (ConfigureViewController *)controller {
  _configureViewController = nil;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell =
      [[self tableView] dequeueReusableCellWithIdentifier:@"UriBeacon"];
  if (cell == nil) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                  reuseIdentifier:@"UriBeacon"];
  }
  UBUriBeacon *beacon = [[_scanner beacons] objectAtIndex:[indexPath row]];
  [[cell textLabel] setText:[[beacon identifier] UUIDString]];
  [[cell detailTextLabel] setText:[[beacon URI] absoluteString]];
  return cell;
}

- (NSInteger)tableView:(UITableView *)tableView
    numberOfRowsInSection:(NSInteger)section {
  return [[_scanner beacons] count];
}

// Settings button handler.
- (void)_openSettings:(id)sender {
  if (_settingsViewController == nil) {
    _settingsViewController =
        [[SettingsViewController alloc] initWithNibName:nil bundle:nil];
  }
  [[self navigationController] pushViewController:_settingsViewController
                                         animated:YES];
}

@end
