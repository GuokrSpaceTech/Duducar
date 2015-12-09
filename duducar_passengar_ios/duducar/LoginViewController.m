//
//  LoginViewController.m
//  duducar
//
//  Created by mactop on 11/16/15.
//  Copyright © 2015 guokrspace. All rights reserved.
//

#import "LoginViewController.h"

#import "DDLog.h"
#import "DDTTYLogger.h"
#import "DDDispatchQueueLogFormatter.h"


// Log levels: off, error, warn, info, verbose
static const int ddLogLevel = LOG_LEVEL_VERBOSE;

#define  SERVER_PORT 8282  // 0 => automatic
#define  SERVER_HOST @"120.24.237.15"

#define USE_SECURE_CONNECTION    0
#define USE_CFSTREAM_FOR_TLS     0 // Use old-school CFStream style technique
#define MANUALLY_EVALUATE_TRUST  0

#define READ_HEADER_LINE_BY_LINE 1


@interface LoginViewController ()<UITextFieldDelegate>
{
    GCDAsyncSocket *asyncSocket;
    RCUnderlineTextField* userNameTextField;
    RCUnderlineTextField* passwordTextField;
    UIButton* verifyCodeButton;
}
@property (retain, nonatomic) IBOutlet RCAnimatedImagesView* animatedImagesView;
@property (nonatomic, strong) UIView* headBackground;
@property (nonatomic, strong) UIImageView* duduLogo;
@property (nonatomic, strong) UIView* inputBackground;
@property (nonatomic, strong) UILabel* errorMsgLb;

@property (weak) NSTimer *repeatingTimer;
@property NSUInteger timerCount;

- (void)countedTimerAction:(NSTimer*)theTimer;
@end

@implementation LoginViewController
@synthesize animatedImagesView = _animatedImagesView;
#define UserTextFieldTag 1000
#define PassWordFieldTag 1001

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
    
    //添加动态背景
    self.animatedImagesView = [[RCAnimatedImagesView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height)];
    [self.view addSubview:self.animatedImagesView];
    self.animatedImagesView.delegate = self;
    
    //添加头部内容
    _headBackground = [[UIView alloc] initWithFrame:CGRectMake(0, -100, self.view.bounds.size.width, 50)];
    _headBackground.userInteractionEnabled = YES;
    _headBackground.backgroundColor = [[UIColor alloc] initWithRed:0 green:0 blue:0 alpha:0.2];
    [self.view addSubview:_headBackground];
    
    //添加图标
    UIImage* duduLogoSmallImage = [UIImage imageNamed:@"title_logo_small"];
    UIImageView* duduLogoSmallImageView = [[UIImageView alloc] initWithFrame:CGRectMake(self.view.bounds.size.width / 2 - 60, 5, 100, 40)];
    [duduLogoSmallImageView setImage:duduLogoSmallImage];
    
    [duduLogoSmallImageView setContentScaleFactor:[[UIScreen mainScreen] scale]];
    duduLogoSmallImageView.contentMode = UIViewContentModeScaleAspectFit;
    duduLogoSmallImageView.autoresizingMask = UIViewAutoresizingFlexibleHeight;
    duduLogoSmallImageView.clipsToBounds = YES;
    [_headBackground addSubview:duduLogoSmallImageView];
    
    UIImage* rongLogoImage = [UIImage imageNamed:@"login_logo"];
    _duduLogo = [[UIImageView alloc] initWithImage:rongLogoImage];
    _duduLogo.contentMode = UIViewContentModeScaleAspectFit;
    _duduLogo.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:_duduLogo];
    
    //中部内容输入区
    _inputBackground = [[UIView alloc] initWithFrame:CGRectZero];
    _inputBackground.translatesAutoresizingMaskIntoConstraints = NO;
    _inputBackground.userInteractionEnabled = YES;
    [self.view addSubview:_inputBackground];
    
    _errorMsgLb = [[UILabel alloc] initWithFrame:CGRectZero];
    _errorMsgLb.text = @"";
    _errorMsgLb.font = [UIFont fontWithName:@"Heiti SC" size:12.0];
    _errorMsgLb.translatesAutoresizingMaskIntoConstraints = NO;
    _errorMsgLb.textColor = [UIColor colorWithRed:204.0f/255.0f green:51.0f/255.0f blue:51.0f/255.0f alpha:1];
    [self.view addSubview:_errorMsgLb];
    
    //用户名
    userNameTextField = [[RCUnderlineTextField alloc] initWithFrame:CGRectZero];
    userNameTextField.backgroundColor = [UIColor clearColor];
    userNameTextField.tag = UserTextFieldTag;
    userNameTextField.delegate=self;
    //_account.placeholder=[NSString stringWithFormat:@"Email"];
    UIColor* color = [UIColor whiteColor];
    userNameTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:@"邮箱" attributes:@{ NSForegroundColorAttributeName : color }];
    userNameTextField.textColor = [UIColor whiteColor];
    userNameTextField.text = [self getDefaultUserName];
    if (userNameTextField.text.length > 0) {
        [userNameTextField setFont:[UIFont fontWithName:@"Heiti SC" size:25.0]];
    }
    userNameTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
    userNameTextField.adjustsFontSizeToFitWidth = YES;
    [userNameTextField addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
    [_inputBackground addSubview:userNameTextField];
    
    verifyCodeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [verifyCodeButton addTarget:self action:@selector(registerMobile:) forControlEvents:UIControlEventTouchUpInside];
    //    [loginButton setBackgroundImage:[UIImage imageNamed:@"login_button"] forState:UIControlStateNormal];
    verifyCodeButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    verifyCodeButton.imageView.contentMode = UIViewContentModeCenter;
    [verifyCodeButton setTitle:@"获取验证码" forState:UIControlStateNormal];
    [verifyCodeButton.titleLabel setFont:[UIFont systemFontOfSize:10]];
    verifyCodeButton.translatesAutoresizingMaskIntoConstraints = NO;
    [_inputBackground addSubview:verifyCodeButton];
    
    //密码
    passwordTextField = [[RCUnderlineTextField alloc] initWithFrame:CGRectZero];
    passwordTextField.tag = PassWordFieldTag;
    passwordTextField.textColor = [UIColor whiteColor];
    passwordTextField.returnKeyType = UIReturnKeyDone;
    passwordTextField.secureTextEntry = YES;
    passwordTextField.delegate=self;
    //passwordTextField.delegate = self;
    passwordTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
    
    passwordTextField.attributedPlaceholder = [[NSAttributedString alloc] initWithString:@"密码" attributes:@{ NSForegroundColorAttributeName : color }];
    //passwordTextField.text = [self getDefaultUserPwd];
    [_inputBackground addSubview:passwordTextField];
    passwordTextField.text = [self getDefaultUserPwd];
    
    UIButton* loginButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [loginButton addTarget:self action:@selector(actionLogin:) forControlEvents:UIControlEventTouchUpInside];
//    [loginButton setBackgroundImage:[UIImage imageNamed:@"login_button"] forState:UIControlStateNormal];
    loginButton.backgroundColor = [UIColor colorWithHexString:@"0195ff" alpha:1.0f];
    loginButton.imageView.contentMode = UIViewContentModeCenter;
    [loginButton setTitle:@"确定" forState:UIControlStateNormal];
    loginButton.translatesAutoresizingMaskIntoConstraints = NO;
    [_inputBackground addSubview:loginButton];
    
    //底部按钮区
    UIView* bottomBackground = [[UIView alloc] initWithFrame:CGRectZero];
    [self.view addSubview:bottomBackground];
    
    bottomBackground.translatesAutoresizingMaskIntoConstraints = NO;
    passwordTextField.translatesAutoresizingMaskIntoConstraints = NO;
    userNameTextField.translatesAutoresizingMaskIntoConstraints = NO;
    
    
    //添加约束
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:bottomBackground attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeBottom multiplier:1.0 constant:20]];
    
    NSDictionary* views = NSDictionaryOfVariableBindings(_errorMsgLb, _duduLogo, _inputBackground, bottomBackground);
    
    NSArray* viewConstraints = [[[[[[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-41-[_inputBackground]-41-|" options:0 metrics:nil views:views]
      arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-14-[_duduLogo]-60-|" options:0 metrics:nil views:views]]
      arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-80-[_duduLogo(==60)]-10-[_errorMsgLb(==10)]-20-[_inputBackground(180)]" options:0 metrics:nil views:views]]
      arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[bottomBackground(==50)]" options:0 metrics:nil views:views]]
      arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-10-[bottomBackground]-10-|" options:0 metrics:nil views:views]]
      arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-40-[_errorMsgLb]-10-|" options:0 metrics:nil views:views]];
    
    [self.view addConstraints:viewConstraints];
    
//    NSLayoutConstraint* userProtocolLabelConstraint = [NSLayoutConstraint constraintWithItem:userProtocolButton attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeCenterX
//                                                                                  multiplier:1.f
//                                                                                    constant:0];
//    [self.view addConstraint:userProtocolLabelConstraint];
    NSDictionary* inputViews = NSDictionaryOfVariableBindings(userNameTextField, verifyCodeButton, passwordTextField, loginButton);
    
    NSArray* inputViewConstraints=[[[[[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[userNameTextField]-20-[verifyCodeButton]|" options:0 metrics:nil views:inputViews]
        arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[verifyCodeButton(==40)]" options:0 metrics:nil views:inputViews]]
        arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[passwordTextField]|" options:0 metrics:nil views:inputViews]]
        arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[userNameTextField(60)]-[passwordTextField(60)]-[loginButton(50)]" options:0 metrics:nil views:inputViews]]
        arrayByAddingObjectsFromArray:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[loginButton]|" options:0 metrics:nil views:inputViews]];
    
    [_inputBackground addConstraints:inputViewConstraints];
    
    
    // AsyncSocket optionally uses the Lumberjack logging framework.
    //
    // Lumberjack is a professional logging framework. It's extremely fast and flexible.
    // It also uses GCD, making it a great fit for GCDAsyncSocket.
    //
    // As mentioned earlier, enabling logging in GCDAsyncSocket is entirely optional.
    // Doing so simply helps give you a deeper understanding of the inner workings of the library (if you care).
    // You can do so at the top of GCDAsyncSocket.m,
    // where you can also control things such as the log level,
    // and whether or not logging should be asynchronous (helps to improve speed, and
    // perfect for reducing interference with those pesky timing bugs in your code).
    //
    // There is a massive amount of documentation on the Lumberjack project page:
    // http://code.google.com/p/cocoalumberjack/
    //
    // But this one line is all you need to instruct Lumberjack to spit out log statements to the Xcode console.
    
    [DDLog addLogger:[DDTTYLogger sharedInstance]];
    
    // We're going to take advantage of some of Lumberjack's advanced features.
    //
    // Format log statements such that it outputs the queue/thread name.
    // As opposed to the not-so-helpful mach thread id.
    //
    // Old : 2011-12-05 19:54:08:161 [17894:f803] Connecting...
    //       2011-12-05 19:54:08:161 [17894:11f03] GCDAsyncSocket: Dispatching DNS lookup...
    //       2011-12-05 19:54:08:161 [17894:13303] GCDAsyncSocket: Creating IPv4 socket
    //
    // New : 2011-12-05 19:54:08:161 [main] Connecting...
    //       2011-12-05 19:54:08:161 [socket] GCDAsyncSocket: Dispatching DNS lookup...
    //       2011-12-05 19:54:08:161 [socket] GCDAsyncSocket: Creating IPv4 socket
    
    DDDispatchQueueLogFormatter *formatter = [[DDDispatchQueueLogFormatter alloc] init];
    [formatter setReplacementString:@"socket" forQueueLabel:GCDAsyncSocketQueueName];
    [formatter setReplacementString:@"socket-cf" forQueueLabel:GCDAsyncSocketThreadName];
    
    [[DDTTYLogger sharedInstance] setLogFormatter:formatter];
    
    [self startSocket];
    

    
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.animatedImagesView startAnimating];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark - animatedImagesView delegate
- (NSUInteger)animatedImagesNumberOfImages:(RCAnimatedImagesView *)animatedImagesView
{
    return 2;
}

- (UIImage *)animatedImagesView:(RCAnimatedImagesView *)animatedImagesView imageAtIndex:(NSUInteger)index
{
    return [UIImage imageNamed:@"login_background.png"];
}

#pragma mark - Local Data Store
/*获取用户账号*/
- (NSString*)getDefaultUserName
{
    NSString* defaultUser = [[NSUserDefaults standardUserDefaults] objectForKey:@"userName"];
    return defaultUser;
}

#pragma mark - selectors
//用户名输入时改变字体大小
- (void)textFieldDidChange:(UITextField*)textField
{
    if (textField.text.length == 0) {
        [textField setFont:[UIFont fontWithName:@"Heiti SC" size:18.0]];
    }
    else {
        [textField setFont:[UIFont fontWithName:@"Heiti SC" size:25.0]];
    }
}

/*获取用户密码*/
- (NSString*)getDefaultUserPwd
{
    NSString* defaultUserPwd = [[NSUserDefaults standardUserDefaults] objectForKey:@"userPwd"];
    return defaultUserPwd;
}

- (IBAction)actionLogin:(id)sender
{
    NSString* userName = [(UITextField*)[self.view viewWithTag:UserTextFieldTag] text];
    NSString* userPwd = [(UITextField*)[self.view viewWithTag:PassWordFieldTag] text];
    
    NSDictionary * postDictionary = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:@"verify", userName, @"2", userPwd ,  nil]
                                                                forKeys:[NSArray arrayWithObjects:@"cmd",      @"mobile",      @"role", @"verifycode", nil]];
    NSError * error = nil;
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
    
    NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [jsonString appendString:@"\n"];
    NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];

    [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
    
    DDLogVerbose(@"Sending Request:\n%@", jsonString);

}

- (IBAction)register:(id)sender
{
    NSString* mobileNumber = [(UITextField*)[self.view viewWithTag:UserTextFieldTag] text];
    
    [self registerMobile:mobileNumber];
}

- (void)registerMobile:(NSString *) mobileNumber
{
    NSString *mobile = userNameTextField.text;
    NSString *phoneRegex = @"^((\\+)|(00)|)[0-9]{11,13}$";
    NSPredicate *phoneTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", phoneRegex];
    
    BOOL phoneValidates = [phoneTest evaluateWithObject:mobile];
    if(phoneValidates)
    {
        NSDictionary * postDictionary = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:@"register",  mobile,   @"2",    nil]
                                                                 forKeys:[NSArray arrayWithObjects:@"cmd",      @"mobile", @"role", nil]];
        
        NSError * error = nil;
        NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
        
        NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        [jsonString appendString:@"\n"];
        NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
        
        [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
        
        DDLogVerbose(@"Sending Request:\n%@", jsonString);
    } else {
        UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
                                                                       message:@"无效的手机号"
                                                                preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
                                                              handler:^(UIAlertAction * action) {}];
        
        [alert addAction:defaultAction];
        [self presentViewController:alert animated:YES completion:nil];
    }
}

#pragma mark - Socket
- (void)startSocket
{
    // Create our GCDAsyncSocket instance.
    //
    // Notice that we give it the normal delegate AND a delegate queue.
    // The socket will do all of its operations in a background queue,
    // and you can tell it which thread/queue to invoke your delegate on.
    // In this case, we're just saying invoke us on the main thread.
    // But you can see how trivial it would be to create your own queue,
    // and parallelize your networking processing code by having your
    // delegate methods invoked and run on background queues.
    
    asyncSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
    
    // Now we tell the ASYNCHRONOUS socket to connect.
    //
    // Recall that GCDAsyncSocket is ... asynchronous.
    // This means when you tell the socket to connect, it will do so ... asynchronously.
    // After all, do you want your main thread to block on a slow network connection?
    //
    // So what's with the BOOL return value, and error pointer?
    // These are for early detection of obvious problems, such as:
    //
    // - The socket is already connected.
    // - You passed in an invalid parameter.
    // - The socket isn't configured properly.
    //
    // The error message might be something like "Attempting to connect without a delegate. Set a delegate first."
    //
    // When the asynchronous sockets connects, it will invoke the socket:didConnectToHost:port: delegate method.
    
    NSError *error = nil;
    
    uint16_t port = SERVER_PORT;
    if (port == 0)
    {
#if USE_SECURE_CONNECTION
        port = 443; // HTTPS
#else
        port = 80;  // HTTP
#endif
    }
    
    if (![asyncSocket connectToHost:SERVER_HOST onPort:port error:&error])
    {
        DDLogError(@"Unable to connect to due to invalid configuration: %@", error);
    }
    else
    {
        DDLogVerbose(@"Connecting to \"%@\" on port %hu...", SERVER_HOST, port);
    }
    
#if USE_SECURE_CONNECTION
    
    // The connect method above is asynchronous.
    // At this point, the connection has been initiated, but hasn't completed.
    // When the connection is established, our socket:didConnectToHost:port: delegate method will be invoked.
    //
    // Now, for a secure connection we have to connect to the HTTPS server running on port 443.
    // The SSL/TLS protocol runs atop TCP, so after the connection is established we want to start the TLS handshake.
    //
    // We already know this is what we want to do.
    // Wouldn't it be convenient if we could tell the socket to queue the security upgrade now instead of waiting?
    // Well in fact you can! This is part of the queued architecture of AsyncSocket.
    //
    // After the connection has been established, AsyncSocket will look in its queue for the next task.
    // There it will find, dequeue and execute our request to start the TLS security protocol.
    //
    // The options passed to the startTLS method are fully documented in the GCDAsyncSocket header file.
    
#if USE_CFSTREAM_FOR_TLS
    {
        // Use old-school CFStream style technique
        
        NSDictionary *options = @{
                                  GCDAsyncSocketUseCFStreamForTLS : @(YES),
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#elif MANUALLY_EVALUATE_TRUST
    {
        // Use socket:didReceiveTrust:completionHandler: delegate method for manual trust evaluation
        
        NSDictionary *options = @{
                                  GCDAsyncSocketManuallyEvaluateTrust : @(YES),
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#else
    {
        // Use default trust evaluation, and provide basic security parameters
        
        NSDictionary *options = @{
                                  GCDAsyncSocketSSLPeerName : CERT_HOST
                                  };
        
        DDLogVerbose(@"Requesting StartTLS with options:\n%@", options);
        [asyncSocket startTLS:options];
    }
#endif
    
#endif
}


- (void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(UInt16)port
{
    DDLogVerbose(@"socket:didConnectToHost:%@ port:%hu", host, port);
    
    // Side Note:
    //
    // The AsyncSocket family supports queued reads and writes.
    //
    // This means that you don't have to wait for the socket to connect before issuing your read or write commands.
    // If you do so before the socket is connected, it will simply queue the requests,
    // and process them after the socket is connected.
    // Also, you can issue multiple write commands (or read commands) at a time.
    // You don't have to wait for one write operation to complete before sending another write command.
    //
    // The whole point is to make YOUR code easier to write, easier to read, and easier to maintain.
    // Do networking stuff when it is easiest for you, or when it makes the most sense for you.
    // AsyncSocket adapts to your schedule, not the other way around.
    
    // Now we tell the socket to read the first line.
    
    [asyncSocket readDataToData:[GCDAsyncSocket LFData] withTimeout:-1.0 tag:0];
    
}

- (void)socket:(GCDAsyncSocket *)sock didReceiveTrust:(SecTrustRef)trust
completionHandler:(void (^)(BOOL shouldTrustPeer))completionHandler
{
    DDLogVerbose(@"socket:shouldTrustPeer:");
    
    dispatch_queue_t bgQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(bgQueue, ^{
        
        // This is where you would (eventually) invoke SecTrustEvaluate.
        // Presumably, if you're using manual trust evaluation, you're likely doing extra stuff here.
        // For example, allowing a specific self-signed certificate that is known to the app.
        
        SecTrustResultType result = kSecTrustResultDeny;
        OSStatus status = SecTrustEvaluate(trust, &result);
        
        if (status == noErr && (result == kSecTrustResultProceed || result == kSecTrustResultUnspecified)) {
            completionHandler(YES);
        }
        else {
            completionHandler(NO);
        }
    });
}

- (void)socketDidSecure:(GCDAsyncSocket *)sock
{
    // This method will be called if USE_SECURE_CONNECTION is set
    
    DDLogVerbose(@"socketDidSecure:");
}

- (void)socket:(GCDAsyncSocket *)sock didWriteDataWithTag:(long)tag
{
    DDLogVerbose(@"socket:didWriteDataWithTag:");
}

- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
    
    NSString *response = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    DDLogVerbose(@"socket:didReadData:withResponse:%@",response);

    
    [asyncSocket readDataWithTimeout:-1 tag:0];
    
    //Deserialastion a Json String into Dictionary
    NSError *jsonError;
    NSData  *objectData = [response dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *responseDict = [NSJSONSerialization JSONObjectWithData:objectData
                                                                 options:NSJSONReadingMutableContainers
                                                                   error:&jsonError];
    NSString *command = [responseDict objectForKey:@"cmd"];
    NSNumber *status =[responseDict objectForKey:@"status"];
    if([command isEqualToString:@"register_resp"])
    {
        if([status intValue]==1)
        {
            //Kick off the timer
            self.timerCount = 30;
            self.repeatingTimer = [NSTimer scheduledTimerWithTimeInterval:1
                                                                   target:self selector:@selector(countedTimerAction:) userInfo:nil repeats:YES];
            [passwordTextField becomeFirstResponder];
        }
        else
        {
            UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
                                                                           message:@"手机未登记"
                                                                    preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
                                                                  handler:^(UIAlertAction * action) {}];
            
            [alert addAction:defaultAction];
            [self presentViewController:alert animated:YES completion:nil];
        }
    }
    else if([command isEqualToString:@"verify_resp"])
    {
        if([status intValue]==1)
        {
            NSString *token = [responseDict objectForKey:@"token"];
            NSString* mobile = [(UITextField*)[self.view viewWithTag:UserTextFieldTag] text];
            
            if(token!=NULL)
            {
                NSDictionary * postDictionary = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:@"login", mobile,  @"2",    token,    nil]
                                                                            forKeys:[NSArray arrayWithObjects:@"cmd",   @"mobile",      @"role", @"token", nil]];
                NSError * error = nil;
                NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
                
                NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                [jsonString appendString:@"\n"];
                NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
                
                [asyncSocket writeData:outStr withTimeout:-1.0 tag:0];
            }
        } else
        {
            UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
                                                                           message:@"验证码错误"
                                                                    preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
                                                                  handler:^(UIAlertAction * action) {}];
            
            [alert addAction:defaultAction];
            [self presentViewController:alert animated:YES completion:nil];
        }
        
    }
    else if([command isEqualToString:@"login_resp"])
    {
        if([status intValue]==1)
        {
            //TODO: 进入主界面
        }
        else
        {
            UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
                                                                           message:@"登陆失败"
                                                                    preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
                                                                  handler:^(UIAlertAction * action) {}];
            
            [alert addAction:defaultAction];
            [self presentViewController:alert animated:YES completion:nil];
        }
    }
    
    

}

- (void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err
{
    //
    
    DDLogVerbose(@"socketDidDisconnect:withError: \"%@\"", err);
}


#pragma mark - TIMER Handles

-(void)countedTimerAction:(NSTimer *)timer
{
    self.timerCount --;
    
    if(self.timerCount<=0)
    {
        [verifyCodeButton setTitle:@"获取验证码" forState:UIControlStateNormal];
        [verifyCodeButton setEnabled:true];
        [timer invalidate];
        self.repeatingTimer = nil;
        return;
    }
    
    NSString *timeLeftStr = [[NSString alloc] initWithFormat:@"      %02d秒      ",(int)_timerCount];
    [verifyCodeButton setTitle:timeLeftStr forState:UIControlStateNormal];
    [verifyCodeButton setEnabled:false];
    
}

@end
