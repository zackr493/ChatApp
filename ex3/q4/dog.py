from .animal import Animal

class Dog(Animal) :
    # no need to redefine num leg and animal type, because inherited from animal

    def sound(self) -> None:
        print("woof")

    def __repr__(self) -> str:
        return f"Dog(num_leg={self.num_leg}, animal_type='{self.animal_type}')"

    pass
