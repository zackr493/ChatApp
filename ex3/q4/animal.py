from abc import ABC, abstractmethod
from typing import Protocol

class AnimalLike(Protocol):
    num_leg : int
    animal_type : str


# multiple inheritance
class Animal(ABC, AnimalLike) :
    def __init__(self, num_leg, animal_type ) -> None:
        self.num_leg = num_leg
        self.animal_type = animal_type

    @abstractmethod
    def sound(self):
        pass

    def print_type(self) -> None:
        print(self.animal_type)




    def __repr__(self) -> str:
        return f"Animal(num_leg={self.num_leg}, animal_type='{self.animal_type}')"





