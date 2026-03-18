from src.circle.simple_circle_class import Circle, PI
import pytest


@pytest.fixture(params=[3], scope="function")
def circle(request):
    return Circle(request.param)


class TestCircle:

    # catch raised exceptions
    def test_negative_radius(self):
        with pytest.raises(ValueError):
            Circle(-3)

    def test_area(self, circle):

        assert circle.area() == PI * (circle.radius * circle.radius)

    def test_perimeter(self, circle):

        assert circle.perimeter() == PI * 2 * circle.radius
