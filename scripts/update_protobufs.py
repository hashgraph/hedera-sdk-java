#!/usr/bin/env python3


import os
import subprocess
import sys


print(">>> Usage: `" + sys.argv[0] + " branch`")
print(">>> Where \"branch\" is a valid branch in the Hedera Protobufs git repo")

if len(sys.argv) != 2:
    print(">>> Incorrect number of arguments.  Exiting.")
    exit()


print("\n\n")


# make sure this is the working directory
def go_to_script_dir():
    os.chdir(os.path.dirname(__file__))


go_to_script_dir()






PROTO_GIT_REMOTE = "https://github.com/hashgraph/hedera-protobufs.git"
PROTO_GIT_PATH = os.path.join("hedera-protos-git")
PROTO_GIT_BRANCH = sys.argv[1]


PROTO_IN_PATH = os.path.join(PROTO_GIT_PATH, "services")
BASIC_TYPES_PATH = os.path.join(PROTO_IN_PATH, "BasicTypes.proto")
RESPONSE_CODE_PATH = os.path.join(PROTO_IN_PATH, "ResponseCode.proto")


MAIN_PATH = os.path.join("..", "sdk", "src", "main")
PROTO_OUT_PATH = os.path.join(MAIN_PATH, "proto")
JAVA_OUT_PATH = os.path.join(MAIN_PATH, "java", "com", "hedera", "hashgraph", "sdk")
REQUEST_TYPE_OUT_PATH = os.path.join(JAVA_OUT_PATH, "RequestType.java")
STATUS_OUT_PATH = os.path.join(JAVA_OUT_PATH, "Status.java")


PROTO_DO_NOT_REMOVE = (
    "TransactionList.proto"
)






COMMENT_REPLACEMENTS = (
    ("&", "and"),
    ("<tt>", ""),
    ("</tt>", "")
)


PROTO_REPLACEMENTS = (
    ("option java_package = \"com.hederahashgraph.api.proto.java\";",
     "option java_package = \"com.hedera.hashgraph.sdk.proto\";"),
    
    ("option java_package = \"com.hederahashgraph.service.proto.java\";",
     "option java_package = \"com.hedera.hashgraph.sdk.proto\";")
)


def do_replacements(s, replacements):
    for r in replacements:
        s = s.replace(r[0], r[1])
    return s






def main():
    ensure_protobufs()
    print(">>> Generating RequestType.java")
    generate_RequestType()
    print(">>> Generating Status.java")
    generate_Status()
    print(">>> Clearing proto output directory")
    clear_proto_dir()
    print(">>> Generating modified proto files")
    generate_modified_protos()
    print(">>> Done")


def ensure_protobufs():
    if os.path.isdir(PROTO_GIT_PATH):
        print(">>> Detected existing protobufs")
    else:
        print(">>> No protobufs detected")
        run_command("git", "clone", PROTO_GIT_REMOTE, PROTO_GIT_PATH)
    os.chdir(PROTO_GIT_PATH)
    run_command("git", "switch", PROTO_GIT_BRANCH)
    run_command("git", "pull", "--rebase")
    go_to_script_dir()

def run_command(*command):
    print(">>> Executing command `" + cmd_to_str(command) + "`")
    if subprocess.run(command).returncode != 0:
        print(">>> Return code was not 0.  Exiting.")
        exit()

def cmd_to_str(command):
    s = ""
    for c in command:
        s += (c + " ")
    return s[0:-1]






def generate_RequestType():
    parse_file(
        BASIC_TYPES_PATH, 
        "HederaFunctionality", 
        add_to_RequestType, 
        finalize_RequestType)
    output_java_file(REQUEST_TYPE_OUT_PATH, RequestType_sections)


def generate_Status():
    parse_file(
        RESPONSE_CODE_PATH, 
        "ResponseCodeEnum", 
        add_to_Status, 
        finalize_Status)
    output_java_file(STATUS_OUT_PATH, Status_sections)


def clear_proto_dir():
    for name in os.listdir(PROTO_OUT_PATH):
        if name in PROTO_DO_NOT_REMOVE:
            continue
        path = os.path.join(PROTO_OUT_PATH, name)
        if os.path.isfile(path):
            os.unlink(path)


def generate_modified_protos():
    for name in os.listdir(PROTO_IN_PATH):
        in_file = open(os.path.join(PROTO_IN_PATH, name), "r")
        out_file = open(os.path.join(PROTO_OUT_PATH, name), "w")
        out_file.write(do_replacements(in_file.read(), PROTO_REPLACEMENTS))
        in_file.close()
        out_file.close()






def premade(name, n):
    return open(os.path.join("premade", name + "-" + str(n) + ".txt"), "r").read()


RequestType_sections = [
    premade("RequestType", 0), 
    "", 
    premade("RequestType", 2), 
    "", 
    premade("RequestType", 4),
    "",
    premade("RequestType", 6)
]


Status_sections = [
    premade("Status", 0),
    "",
    premade("Status", 2),
    "",
    premade("Status", 4),
]


def output_java_file(out_path, section_list):
    out_file = open(out_path, "w")
    for section in section_list:
        out_file.write(section)
    out_file.close()






def add_to_RequestType(original_name, cap_snake_name, comment_lines):
    RequestType_sections[1] += \
        generate_enum(original_name, cap_snake_name, comment_lines, "HederaFunctionality", 1)
    RequestType_sections[3] += generate_valueOf(original_name, cap_snake_name, 3)
    RequestType_sections[5] += generate_toString(original_name, cap_snake_name, 3)


def add_to_Status(original_name, cap_snake_name, comment_lines):
    Status_sections[1] += \
        generate_enum(original_name, cap_snake_name, comment_lines, "ResponseCodeEnum", 1)
    Status_sections[3] += generate_valueOf(original_name, cap_snake_name, 3)


def replace_last_enum_comma(s):
    return s[0:-3] + ";\n\n"


def finalize_RequestType():
    RequestType_sections[1] = replace_last_enum_comma(RequestType_sections[1])


def finalize_Status():
    Status_sections[1] = replace_last_enum_comma(Status_sections[1])






def tabs(n):
    return " "*(4*n)


def generate_comment(comment_lines, tab_count):
    if(len(comment_lines) > 0):
        retval = tabs(tab_count) + "/**\n"
        for line in comment_lines:
            retval += tabs(tab_count) + " * " + \
                do_replacements(line, COMMENT_REPLACEMENTS) + "\n"
        return retval + tabs(tab_count) + " */\n"
    else:
        return ""


def generate_enum_line(original_name, cap_snake_name, enum_name, tab_count):
    return tabs(tab_count) + cap_snake_name + \
        "(" + enum_name + "." + original_name + "),\n\n"


def generate_enum(original_name, cap_snake_name, comment_lines, enum_name, tabs_count):
    return generate_comment(comment_lines, tabs_count) + \
        generate_enum_line(original_name, cap_snake_name, enum_name, tabs_count)


def generate_valueOf(original_name, cap_snake_name, tabs_count):
    return tabs(tabs_count) + "case " + original_name + ":\n" + \
        tabs(tabs_count + 1) + "return " + cap_snake_name + ";\n"


def generate_toString(original_name, cap_snake_name, tabs_count):
    return tabs(tabs_count) + "case " + cap_snake_name + ":\n" + \
        tabs(tabs_count + 1) + "return \"" + cap_snake_name + "\";\n"






def parse_file(in_path, enum_name, add_to_output, finalize_output):
    in_file = open(in_path, "r")
    s = in_file.read()
    in_file.close()
    enum_i = s.find("enum " + enum_name, 0)
    i = s.find("{", enum_i) + 1
    j = s.find("}", i)
    while i < j:
        equal_i = s.find("=", i)
        comment_i = s.find("//", i)
        end_i = s.find("\n", i)
        if equal_i >= j:
            break
        comment_lines = []
        # Normal line
        if equal_i < comment_i and comment_i < end_i:
            comment_lines = get_comment(s, comment_i, end_i, comment_lines)
        # Empty line
        elif equal_i > end_i and comment_i > end_i:
            i = end_i + 1
            continue
        # multi-line comment followed by line,
        # or line with no comment
        else:
            while equal_i > end_i:
                comment_lines = get_comment(s, comment_i, end_i, comment_lines)
                i = end_i + 1
                comment_i = s.find("//", i)
                end_i = s.find("\n", i)
        original_name = s[i:equal_i].strip()
        cap_snake_name = ensure_cap_snake_name(original_name)
        add_to_output(original_name, cap_snake_name, comment_lines)
        i = end_i + 1
    finalize_output()


def id_is_next(name, i):
    if (i + 1) < len(name):
        return name[i:i+2] == "ID"
    return False


def ensure_cap_snake_name(name):
    # assume that name is snake-case if it contains a _ or is not mixed-case
    has_underscore = "_" in name
    is_not_mixed = name.isupper() or name.islower()
    if has_underscore or (not has_underscore and is_not_mixed):
        return name.upper()
    else:
        out = name[0].upper()
        i = 1
        while i < len(name):
            if id_is_next(name, i):
                out += "_ID"
                i += 2
            else:
                c = name[i]
                if c.isupper():
                    out += "_"
                out += c.upper()
                i += 1
        return out


def get_comment(s, comment_i, end_i, comment_lines):
    if comment_i < end_i:
        return comment_lines + [s[comment_i + 2:end_i].strip()]
    else:
        return comment_lines






main()


