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

#import "AppDelegate.h"
#import "ViewController.h"

@interface AppDelegate ()

@end

@implementation AppDelegate {
  UINavigationController *_navigationController;
}

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];

  ViewController *viewController =
      [[ViewController alloc] initWithNibName:nil bundle:nil];
  _navigationController = [[UINavigationController alloc]
      initWithRootViewController:viewController];
  self.window.rootViewController = _navigationController;
  [self.window makeKeyAndVisible];

  UIUserNotificationSettings *settings =
      [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert
                                        categories:nil];
  [[UIApplication sharedApplication] registerUserNotificationSettings:settings];

  return YES;
}

@end
