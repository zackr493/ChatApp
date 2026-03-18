import string

PI = 3.14

# when running Pylint, we get
# ************ Module src.simple_circle_class
# src/simple_circle_class.py:23:0: C0305: Trailing newlines (trailing-newlines)
# src/simple_circle_class.py:1:0: C0114: Missing module docstring (missing-module-docstring)
# src/simple_circle_class.py:7:0: C0115: Missing class docstring (missing-class-docstring)
# src/simple_circle_class.py:13:4: C0116: Missing function or method docstring (missing-function-docstring)
# src/simple_circle_class.py:16:4: C0116: Missing function or method docstring (missing-function-docstring)
# src/simple_circle_class.py:3:0: W0611: Unused import string (unused-import)
#
#                                                             -----------------------------------
# Your code has been rated at 5.00/10


class Circle:

    def __init__(self, radius: int) -> None:
        #  simulate catching raise exceptions
        if radius < 0:
            raise ValueError("Radius cannot be negative")
        self.radius = radius

    def area(self) -> str:
        return PI * self.radius**2

    def perimeter(self) -> str:
        return PI * 2 * self.radius # type: ignore

    def __repr__(self):
        return f"{self.__class__.__name__}(radius={self.radius})"
