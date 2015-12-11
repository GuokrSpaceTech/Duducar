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
#import "DDSocket.h"
#import "DDMainViewController.h"
#import "DDDatabase.h"


// Log levels: off, error, warn, info, verbose
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
static NSString * responseNotificationName = @"DDSocketResponseNotification";

#define  SERVER_PORT 8282  // 0 => automatic
#define  SERVER_HOST @"120.24.237.15"

#define USE_SECURE_CONNECTION    0
#define USE_CFSTREAM_FOR_TLS     0 // Use old-school CFStream style technique
#define MANUALLY_EVALUATE_TRUST  0

#define READ_HEADER_LINE_BY_LINE 1

static const CGFloat KEYBOARD_ANIMATION_DURATION = 0.3;
static const CGFloat MINIMUM_SCROLL_FRACTION = 0.2;
static const CGFloat MAXIMUM_SCROLL_FRACTION = 0.8;
static const CGFloat PORTRAIT_KEYBOARD_HEIGHT = 216;
static const CGFloat LANDSCAPE_KEYBOARD_HEIGHT = 162;


@interface LoginViewController ()<UITextFieldDelegate>
{
    GCDAsyncSocket *asyncSocket;
    RCUnderlineTextField* userNameTextField;
    RCUnderlineTextField* passwordTextField;
    UIButton* verifyCodeButton;
    CGFloat animatedDistance;
    NSString *mobile;
    NSString *token;
}
@property (strong, nonatomic) RCAnimatedImagesView* animatedImagesView;
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
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(receiveResponseHandles:) name:responseNotificationName object:nil];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.animatedImagesView startAnimating];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

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

    [[DDSocket currentSocket] sendData:outStr timeOut:-1 tag:0];
    
}

- (IBAction)register:(id)sender
{
    NSString* mobileNumber = [(UITextField*)[self.view viewWithTag:UserTextFieldTag] text];
    
    [self registerMobile:mobileNumber];
}

- (void)registerMobile:(NSString *) mobileNumber
{
    mobile = userNameTextField.text;
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
        
        [[DDSocket currentSocket] sendData:outStr timeOut:-1.0 tag:0];

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

-(void)receiveResponseHandles:(NSNotification *)notification
{
    NSDictionary *responseDict = notification.userInfo;
    
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
            token = [responseDict objectForKey:@"token"];
//            mobile = [(UITextField*)[self.view viewWithTag:UserTextFieldTag] text];
            
            if(token!=NULL)
            {
                NSDictionary * postDictionary = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:@"login", mobile,  @"2",    token,    nil]
                                                                            forKeys:[NSArray arrayWithObjects:@"cmd",   @"mobile",  @"role",  @"token", nil]];
                NSError * error = nil;
                NSData * jsonData = [NSJSONSerialization dataWithJSONObject:postDictionary options:NSUTF8StringEncoding error:&error];
                
                NSMutableString *jsonString = [[NSMutableString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
                [jsonString appendString:@"\n"];
                NSData *outStr = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
                
                [[DDSocket currentSocket] sendData:outStr timeOut:-1 tag:0];
            }
        } else
        {
//            UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
//                                                                           message:@"验证码错误"
//                                                                    preferredStyle:UIAlertControllerStyleAlert];
//            UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
//                                                                  handler:^(UIAlertAction * action) {}];
//            
//            [alert addAction:defaultAction];
//            [self presentViewController:alert animated:YES completion:nil];
        }
        
    }
    else if([command isEqualToString:@"login_resp"])
    {
        if([status intValue]==1)
        {
            [[DDDatabase sharedDatabase] insertDataToPersonInfoTableToken:token phone:mobile];
            
            DDMainViewController *mainVC = [[DDMainViewController alloc]init];
            [self.navigationController pushViewController:mainVC animated:YES];
        }
        else
        {
//            UIAlertController* alert = [UIAlertController alertControllerWithTitle:@""
//                                                                       message:@"登陆失败"
//                                                                 preferredStyle:UIAlertControllerStyleAlert];
//            UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:@"我知道了" style:UIAlertActionStyleDefault
//                                                                  handler:^(UIAlertAction * action) {}];
//            
//            [alert addAction:defaultAction];
//            [self presentViewController:alert animated:YES completion:nil];
        }
    }

}

#pragma mark - Softkey Up and hide handles

- (void)textFieldDidBeginEditing:(UITextField *)textField{
    CGRect textFieldRect =
    [self.view.window convertRect:textField.bounds fromView:textField];
    CGRect viewRect =
    [self.view.window convertRect:self.view.bounds fromView:self.view];
    CGFloat midline = textFieldRect.origin.y + 0.5 * textFieldRect.size.height;
    CGFloat numerator =
    midline - viewRect.origin.y
    - MINIMUM_SCROLL_FRACTION * viewRect.size.height;
    CGFloat denominator =
    (MAXIMUM_SCROLL_FRACTION - MINIMUM_SCROLL_FRACTION)
    * viewRect.size.height;
    CGFloat heightFraction = numerator / denominator;
    if (heightFraction < 0.0)
    {
        heightFraction = 0.0;
    }
    else if (heightFraction > 1.0)
    {
        heightFraction = 1.0;
    }
    UIInterfaceOrientation orientation =
    [[UIApplication sharedApplication] statusBarOrientation];
    if (orientation == UIInterfaceOrientationPortrait ||
        orientation == UIInterfaceOrientationPortraitUpsideDown)
    {
        animatedDistance = floor(PORTRAIT_KEYBOARD_HEIGHT * heightFraction);
    }
    else
    {
        animatedDistance = floor(LANDSCAPE_KEYBOARD_HEIGHT * heightFraction);
    }
    CGRect viewFrame = self.view.frame;
    viewFrame.origin.y -= animatedDistance;
    
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationBeginsFromCurrentState:YES];
    [UIView setAnimationDuration:KEYBOARD_ANIMATION_DURATION];
    
    [self.view setFrame:viewFrame];
    
    [UIView commitAnimations];
}

- (void)textFieldDidEndEditing:(UITextField *)textfield{
    
    CGRect viewFrame = self.view.frame;
    viewFrame.origin.y += animatedDistance;
    
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationBeginsFromCurrentState:YES];
    [UIView setAnimationDuration:KEYBOARD_ANIMATION_DURATION];
    
    [self.view setFrame:viewFrame];
    
    [UIView commitAnimations];
}

- (void)keyboardDidShow:(NSNotification *)notification
{
    // Assign new frame to your view
    CGRect oldFrame = self.view.frame;
    CGRect newFrame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y - 110,
                                 oldFrame.size.width, oldFrame.size.height);
    
    [self.view setFrame:newFrame];
    
}

-(void)keyboardDidHide:(NSNotification *)notification
{
    CGRect oldFrame = self.view.frame;
    CGRect newFrame = CGRectMake(oldFrame.origin.x, oldFrame.origin.y + 110,
                                 oldFrame.size.width, oldFrame.size.height);
    [self.view setFrame:newFrame];
}


@end
