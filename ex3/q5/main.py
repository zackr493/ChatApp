def add_there(func):
    return func + " there"

def uppercase(text) :
    return text.upper()

class String :
    def __init__(self, value):
        self.value = value

    def __add__(self, other):
        return f"{self.value} not {other}"


if __name__ == "__main__"  :

    # higher order functions
    print(add_there(uppercase("hello")))

    # dunder methods, we override it
    s1 = String("hello")
    print(s1 + "world")
    pass

