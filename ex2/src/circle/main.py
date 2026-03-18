from .simple_circle_class import Circle


def run_app():
    """Entry point for wheel"""
    print("starting circle app...")

    circle = Circle(radius=5)

    print(f"The area is: {circle.area()}")


    # mypy

    circle2 = Circle(radius=5)








if __name__ == "__main__":
    run_app()
