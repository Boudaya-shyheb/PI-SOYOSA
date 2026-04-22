import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { MapLocationComponent } from './map-location.component';
import { ElementRef } from '@angular/core';

describe('MapLocationComponent', () => {
  let component: MapLocationComponent;
  let fixture: ComponentFixture<MapLocationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MapLocationComponent ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MapLocationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize map on view init', fakeAsync(() => {
    fixture.detectChanges();
    tick(100); // setTimeout for initMap
    
    // Check if map property is defined
    expect((component as any).map).toBeDefined();
  }));

  it('should place marker if latitude and longitude provided', fakeAsync(() => {
    component.latitude = 10;
    component.longitude = 20;
    
    fixture.detectChanges();
    tick(100);
    
    expect((component as any).marker).toBeDefined();
    const latLng = (component as any).marker.getLatLng();
    expect(latLng.lat).toBe(10);
    expect(latLng.lng).toBe(20);
  }));

  it('should emit locationSelected on map click if not readOnly', fakeAsync(() => {
    component.readOnly = false;
    spyOn(component.locationSelected, 'emit');
    
    fixture.detectChanges();
    tick(100);
    
    const mockEvent = {
      latlng: { lat: 15, lng: 25 }
    };
    
    // Simulate Leaflet map click
    (component as any).map.fire('click', mockEvent);
    
    expect(component.locationSelected.emit).toHaveBeenCalledWith({ lat: 15, lng: 25 });
  }));

  it('should not emit locationSelected on map click if readOnly', fakeAsync(() => {
    component.readOnly = true;
    spyOn(component.locationSelected, 'emit');
    
    fixture.detectChanges();
    tick(100);
    
    const mockEvent = { latlng: { lat: 15, lng: 25 } };
    (component as any).map.fire('click', mockEvent);
    
    expect(component.locationSelected.emit).not.toHaveBeenCalled();
  }));

  it('should handle geolocation success in getDirectionsOnMap', fakeAsync(() => {
    const mockPosition = {
      coords: { latitude: 50, longitude: 60 }
    };
    spyOn(navigator.geolocation, 'watchPosition').and.callFake((success: any) => {
      success(mockPosition);
      return 1;
    });
    
    component.latitude = 10;
    component.longitude = 20;
    component.readOnly = true;

    component.getDirectionsOnMap();
    
    expect(component.isLocating).toBeTrue();
    // Verification of drawRoute call (private) can be done by checking layer counts or markers
  }));
});
