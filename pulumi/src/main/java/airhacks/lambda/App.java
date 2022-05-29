package airhacks.lambda;

import com.pulumi.Pulumi;
import com.pulumi.asset.FileArchive;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.iam.RolePolicy;
import com.pulumi.aws.iam.RolePolicyArgs;
import com.pulumi.aws.iam.RolePolicyAttachment;
import com.pulumi.aws.iam.RolePolicyAttachmentArgs;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.lambda.FunctionArgs;
import com.pulumi.aws.lambda.FunctionUrl;
import com.pulumi.aws.lambda.FunctionUrlArgs;
import com.pulumi.aws.lambda.enums.Runtime;

public class App {

    static String lambdaHandler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest";

    static Role createRole() {
        var assumeRolePolicy = """
            {
                "Version": "2012-10-17",
                "Statement": [
                  {
                    "Effect": "Allow",
                    "Principal": {
                      "Service": "lambda.amazonaws.com"
                    },
                    "Action": "sts:AssumeRole"
                  }
                ]
              }                
                """;        

        var managedPolicy = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole";
        var role = new Role("ExecutionRole", RoleArgs.builder()
                .managedPolicyArns(managedPolicy)
                .assumeRolePolicy(assumeRolePolicy)
                .build());
        

        var policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Action": "lambda:InvokeFunctionUrl",
                            "Resource": "arn:aws:lambda:eu-central-1:*:function:*",
                            "Condition": {
                                "StringEquals": {
                                    "lambda:FunctionUrlAuthType": "NONE"
                                }
                            }
                        }
                    ]
                }
                """;
        var rolePolicy = new RolePolicy("RolePolicy", RolePolicyArgs.builder()
                .policy(policy)
                .role(role.getId())
                .build());

        var roleAttachment = new RolePolicyAttachment("ManagedRole", RolePolicyAttachmentArgs.builder()
                .policyArn("arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole")
                .role(role.getId())
                .build());
        return role;

    }

    static Function createFunction(String functionName,int memory,int timeout) {
        var role = createRole();
        return new Function("quarkus-lambda", FunctionArgs.builder()
                .name(functionName)
                .description("Quarkus with Pulumi")
                .role(role.arn())
                .code(new FileArchive("../lambda/target/function.zip"))
                .handler(lambdaHandler)
                .architectures("arm64")
                .memorySize(memory)
                .timeout(timeout)
                .runtime(Runtime.Java11)
                .build());

    }

    public static void main(String[] args) {
        var functionName = "airhacks_QuarkusAndPulumi";
        var memory = 1024;
        var timeout = 30;

        Pulumi.run(ctx -> {
            var function = createFunction(functionName,memory,timeout);
            var functionUrl = new FunctionUrl("FunctionURL", FunctionUrlArgs.builder()
                    .functionName(function.name())
                    .authorizationType("NONE")
                    .build());

            ctx.export("functionUrl", functionUrl.functionUrl().applyValue(url -> "curl " + url + "hello"));
            ctx.export("functionName", function.name());
        });
    }
}
