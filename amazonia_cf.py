#!/usr/bin/env python

# pylint: disable=missing-docstring, invalid-name, line-too-long, redefined-outer-name, too-many-arguments
import sys
from amazonia.cftemplates import *
from amazonia.amazonia_resources import *

def main(args):
    keypair = args[0]
    appname = args[1]
    bootstrap_location = args[2]

    bootstrap_file= open(bootstrap_location)
    userdata = bootstrap_file.read()

    template = DualAZenv(keypair)

    HTTP_PORT="8080"      # This variable is used to specify the port for general tomcat HTTP traffic
    HTTPS_PORT="443"    # This variable is used to specify the port for general HTTPS traffic
    SSH_PORT="22"       # SSH Port

    elb_sg = add_security_group(template, template.vpc)
    web_sg = add_security_group(template, template.vpc)

    # NAT Rules
    # all from web security group
    # all to public
    add_security_group_ingress(template, template.nat_security_group, "-1", "-1", "-1", source_security_group=web_sg)
    add_security_group_egress(template, template.nat_security_group, "-1", "-1", "-1", cidr=PUBLIC_CIDR)

    # ELB Rules
    # 80 and 443 from public
    # all to public
    add_security_group_ingress(template, elb_sg, "tcp", HTTP_PORT, HTTP_PORT, cidr=PUBLIC_CIDR)
    add_security_group_ingress(template, elb_sg, "tcp", HTTPS_PORT, HTTPS_PORT, cidr=PUBLIC_CIDR)
    add_security_group_egress(template, elb_sg, "-1", "-1", "-1", destination_security_group=web_sg)

    # WEB Rules
    # 80, 443, and 8080 from ELB security group
    # all to NAT security group
    add_security_group_ingress(template, web_sg, "tcp", HTTP_PORT, HTTP_PORT, cidr=PUBLIC_CIDR)
    add_security_group_ingress(template, web_sg, "tcp", HTTPS_PORT, HTTPS_PORT, cidr=PUBLIC_CIDR)
    add_security_group_egress(template, web_sg, "-1", "-1", "-1", cidr=PUBLIC_CIDR)
    add_security_group_ingress(template, web_sg, "tcp", SSH_PORT, SSH_PORT, cidr=PUBLIC_CIDR)

    elb = add_load_balancer(template, [template.public_subnet1, template.public_subnet2], 'HTTP:8080/index.html', [elb_sg], dependson=[template.internet_gateway_attachment])

    web_launch_config = add_launch_config(template, keypair, [web_sg], WEB_IMAGE_ID, WEB_INSTANCE_TYPE, userdata=userdata)
    web_asg = add_auto_scaling_group(template, 2, [template.public_subnet1, template.public_subnet2], launch_configuration=web_launch_config, health_check_type="ELB", load_balancer=elb, dependson=[template.internet_gateway], multiAZ=True, app_name=appname)

    print(template.to_json(indent=2, separators=(',', ': ')))

if __name__ == "__main__":
    main(sys.argv[1:])