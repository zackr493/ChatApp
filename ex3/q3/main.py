class Foo  :

    def __init__ (self, a: str, b: str, c: str) -> None:
        self.a = a
        self.b = b
        self.c = c


    def __call__(self, a: str, b: str, c: str) -> None:

        print(a,b,c)

    def __repr__(self):
        return f"Foo(a='{self.a}', b='{self.b}', c='{self.c}')"



if __name__ == "__main__"  :

    print("__init__ initializes an object")
    foo = Foo("a","b","c")
    print(foo)

    print("\n\n")
    print("__call__ implements a function call operator, it can only be called when the object is initialized")
    foo("a" , "b", "c")





