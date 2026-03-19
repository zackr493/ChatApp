def apply(func, x):
    return func(x)

def add(text) :
    return text + text

class String :
    def __init__(self, value):
        self.value = value

    def __add__(self, other):
        return f"{self.value} not {other}"


if __name__ == "__main__"  :

    # higher order functions
    print(apply(add, "test "))

    # dunder methods, we override it
    s1 = String("hello")
    print(s1 + "world")
    pass

