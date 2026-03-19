import sys
import os.path


def hello() -> str:
    return "Hello"

def count_file(file_name) -> int :
    # this function counts the number of lines in files
    # exclu. comments , blank lines

    total_lines = 0

    with open(file_name, "r" , encoding="utf-8") as f :

        for l in f :
            l = l.strip()
            if len(l) == 0 :
                continue
            if l.startswith("#") or l.startswith(('"""' , "'''")):
                continue
            total_lines += 1


        return total_lines

if __name__ == "__main__":

    if len(sys.argv) != 2 :
        sys.exit("Please input name of file: 'Python -m q2.src.main <filename>'")

    file_name = sys.argv[1]


    if not file_name.endswith('.py') :
        sys.exit("Argument must be a '.py' file")

    if not os.path.isfile(file_name) :
        sys.exit("File does not exists")


    print(count_file(file_name))


